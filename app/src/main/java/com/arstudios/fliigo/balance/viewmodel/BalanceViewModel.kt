package com.arstudios.fliigo.balance.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.R
import com.arstudios.fliigo.inventory.data.ProductDto
import com.arstudios.fliigo.balance.data.*
import com.arstudios.fliigo.core.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface BalanceUiState {
    object Loading : BalanceUiState
    data class Success(
        val storeName: String,
        val category: String,
        val balance: Double,
        val totalIncome: Double,
        val totalExpenses: Double,
        val salesList: List<SaleItem> = emptyList()
    ) : BalanceUiState
    data class Error(val message: String) : BalanceUiState
}

class BalanceViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "BalanceViewModel"
    }

    var uiState: BalanceUiState by mutableStateOf(BalanceUiState.Loading)
        private set

    var productList by mutableStateOf<List<ProductDto>>(emptyList())
        private set

    var isLoadingProducts by mutableStateOf(false)
        private set

    var productErrorMessage by mutableStateOf<String?>(null)
        private set

    var activeRowIndex by mutableStateOf<Int?>(null)
        private set

    var showScannerModal by mutableStateOf(false)
        private set

    var lastScannedBarcode by mutableStateOf<Pair<Int, String>?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        loadStoreBalance()
        loadStoreProducts()
    }

    fun updateActiveRowIndex(index: Int?) { activeRowIndex = index }

    fun setScannerModalVisibility(isVisible: Boolean) {
        showScannerModal = isVisible
        if (!isVisible) activeRowIndex = null
    }

    fun deliverScannedCode(index: Int, code: String) { lastScannedBarcode = Pair(index, code) }

    fun clearScannedBarcode() { lastScannedBarcode = null }

    fun loadStoreBalance(refreshing: Boolean = false) {
        viewModelScope.launch {
            if (refreshing) isRefreshing = true else uiState = BalanceUiState.Loading
            
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("USER_ID", -1)

                if (userId == -1) {
                    uiState = BalanceUiState.Error(getApplication<Application>().getString(R.string.error_user_not_identified))
                    return@launch
                }

                val response = RetrofitClient.instance.getStoreBalance(userId)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val today = sdf.format(Date())

                    val mappedSales = data.sales?.map { dto ->
                        SaleItem(
                            id = dto.id ?: 0,
                            productName = dto.productName ?: "Varios productos",
                            clientName = dto.clientName ?: "Cliente general",
                            amount = dto.amount ?: 0.0,
                            date = dto.date ?: "",
                            address = dto.address ?: "",
                            phone = dto.phone ?: "",
                            quantity = dto.quantity ?: 1
                        )
                    }?.filter { it.date.startsWith(today) } ?: emptyList()

                    uiState = BalanceUiState.Success(
                        storeName = data.storeName ?: "Tienda AR",
                        category = data.category ?: "",
                        balance = data.balance ?: 0.0,
                        totalIncome = data.totalIncome ?: 0.0,
                        totalExpenses = data.totalExpenses ?: 0.0,
                        salesList = mappedSales
                    )
                } else {
                    uiState = BalanceUiState.Error(getApplication<Application>().getString(R.string.error_balance_load_failed))
                }
            } catch (e: Exception) {
                uiState = BalanceUiState.Error(getApplication<Application>().getString(R.string.error_network_generic, e.localizedMessage))
            } finally {
                isRefreshing = false
            }
        }
    }

    fun registerSaleWithValidation(
        clientName: String,
        address: String,
        phone: String,
        productRows: List<com.arstudios.fliigo.balance.ui.components.SaleItemRow>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val app = getApplication<Application>()
        if (clientName.isBlank()) {
            onError(app.getString(R.string.error_empty_client))
            return
        }

        val saleItems = mutableListOf<SaleItemRequest>()
        for (row in productRows) {
            val inputCode = row.productId.trim()
            val requestedQty = row.quantity.toIntOrNull() ?: 0

            if (inputCode.isBlank() || requestedQty <= 0) {
                onError(app.getString(R.string.error_invalid_prod_qty))
                return
            }

            var matchedProduct = productList.find { it.barcode == inputCode }
            if (matchedProduct == null) {
                val pIdInt = inputCode.toIntOrNull()
                if (pIdInt != null) {
                    matchedProduct = productList.find { it.id == pIdInt }
                }
            }

            if (matchedProduct == null) {
                onError(app.getString(R.string.error_product_not_found) + ": $inputCode")
                return
            }

            if (matchedProduct.stock < requestedQty) {
                onError(app.getString(R.string.error_insufficient_stock, matchedProduct.name, matchedProduct.stock))
                return
            }
            
            matchedProduct.id?.let { 
                saleItems.add(SaleItemRequest(it, requestedQty))
            }
        }

        if (saleItems.isEmpty()) {
            onError("No hay productos válidos para registrar")
            return
        }

        registerSale(clientName, address, phone, saleItems, onSuccess, onError)
    }

    private fun registerSale(
        clientName: String,
        address: String,
        phone: String,
        items: List<SaleItemRequest>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    onError(getApplication<Application>().getString(R.string.error_store_not_identified))
                    return@launch
                }

                val request = SaleRequest(
                    storeId = storeId,
                    clientName = clientName,
                    address = address,
                    phone = phone,
                    items = items
                )

                val response = RetrofitClient.instance.registerSale(request)
                if (response.isSuccessful) {
                    onSuccess()
                    delay(500)
                    loadStoreBalance()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error del servidor"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                onError(getApplication<Application>().getString(R.string.error_network_register_sale, e.localizedMessage))
            }
        }
    }

    fun loadStoreProducts() {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)
                if (storeId != -1) {
                    isLoadingProducts = true
                    val response = RetrofitClient.instance.getProductsByStore(storeId)
                    if (response.isSuccessful) {
                        productList = response.body() ?: emptyList()
                    }
                }
            } catch (e: Exception) { /* Silencio */ }
            finally { isLoadingProducts = false }
        }
    }

    fun deleteSale(saleId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteSale(saleId)
                if (response.isSuccessful) {
                    onSuccess()
                    loadStoreBalance()
                } else {
                    onError("No se pudo eliminar la venta.")
                }
            } catch (e: Exception) {
                onError("Error de red: ${e.localizedMessage}")
            }
        }
    }

    fun cerrarSesionLocal(onLoggedOut: (() -> Unit)? = null) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        RetrofitClient.context = null
        onLoggedOut?.invoke()
    }
}
