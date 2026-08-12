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
import com.arstudios.fliigo.balance.data.SaleItem
import com.arstudios.fliigo.balance.data.SaleRequest
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

    // Control de índice activo para el escáner de códigos de barras en los diálogos de venta
    var activeRowIndex by mutableStateOf<Int?>(null)
        private set

    // Estado para controlar la visibilidad del modal del escáner en la pantalla
    var showScannerModal by mutableStateOf(false)
        private set

    // Estado reactivo para propagar el código escaneado junto con su índice de fila
    var lastScannedBarcode by mutableStateOf<Pair<Int, String>?>(null)
        private set

    init {
        Log.d(TAG, "Inicializando BalanceViewModel. Llamando a loadStoreBalance()...")
        loadStoreBalance()
        loadStoreProducts()
    }

    fun updateActiveRowIndex(index: Int?) {
        activeRowIndex = index
    }

    fun setScannerModalVisibility(isVisible: Boolean) {
        showScannerModal = isVisible
        if (!isVisible) {
            activeRowIndex = null
        }
    }

    fun deliverScannedCode(index: Int, code: String) {
        lastScannedBarcode = Pair(index, code)
    }

    fun clearScannedBarcode() {
        lastScannedBarcode = null
    }

    fun loadStoreBalance() {
        viewModelScope.launch {
            Log.d(TAG, "=== INICIO loadStoreBalance ===")
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getInt("USER_ID", -1)
                Log.d(TAG, "USER_ID recuperado de SharedPreferences: $userId")

                if (userId == -1) {
                    uiState = BalanceUiState.Error(getApplication<Application>().getString(R.string.error_user_not_identified))
                    Log.e(TAG, "Error: UserId no encontrado en SharedPreferences.")
                    return@launch
                }

                val response = RetrofitClient.instance.getStoreBalance(userId)

                if (response.code() == 401 || response.code() == 403) {
                    cerrarSesionLocal()
                    uiState = BalanceUiState.Error(getApplication<Application>().getString(R.string.error_session_expired))
                    return@launch
                }

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val today = sdf.format(Date())
                    Log.d(TAG, "Filtrando ventas para el día: $today")

                    val mappedSales = data.sales?.map { dto ->
                        SaleItem(
                            id = dto.id ?: 0,
                            productName = dto.productName ?: "Sin producto",
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
                Log.d(TAG, "=== FIN loadStoreBalance ===")
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

        if (productRows.isEmpty() || productRows.any { it.productId.isBlank() }) {
            onError(app.getString(R.string.error_no_products))
            return
        }

        for (row in productRows) {
            val pIdInt = row.productId.toIntOrNull()
            val requestedQty = row.quantity.toIntOrNull() ?: 0

            if (pIdInt == null || requestedQty <= 0) {
                onError(app.getString(R.string.error_invalid_prod_qty))
                return
            }

            val matchedProduct = productList.find { it.id == pIdInt }
            if (matchedProduct == null) {
                onError(app.getString(R.string.error_product_not_found))
                return
            }

            if (matchedProduct.stock < requestedQty) {
                onError(app.getString(R.string.error_insufficient_stock, matchedProduct.name, matchedProduct.stock))
                return
            }
        }

        val firstRow = productRows.first()
        registerSale(
            clientName = clientName,
            address = address,
            phone = phone,
            productId = firstRow.productId.toInt(),
            quantity = firstRow.quantity.toInt(),
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun registerSale(
        clientName: String,
        address: String,
        phone: String,
        productId: Int,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = sharedPreferences.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    onError(getApplication<Application>().getString(R.string.error_store_not_identified))
                    return@launch
                }

                val request = SaleRequest(
                    storeId = storeId,
                    clientName = clientName,
                    address = address,
                    phone = phone,
                    productId = productId,
                    quantity = quantity
                )

                val response = RetrofitClient.instance.registerSale(request)

                if (response.code() == 401 || response.code() == 403) {
                    cerrarSesionLocal()
                    onError(getApplication<Application>().getString(R.string.error_session_expired))
                    return@launch
                }

                if (response.isSuccessful) {
                    onSuccess()
                    delay(500)
                    loadStoreBalance()
                    loadStoreProducts()
                } else {
                    onError(getApplication<Application>().getString(R.string.error_register_sale_failed))
                }

            } catch (e: Exception) {
                onError(getApplication<Application>().getString(R.string.error_network_register_sale, e.localizedMessage))
            }
        }
    }

    fun loadStoreProducts() {
        viewModelScope.launch {
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = sharedPreferences.getInt("STORE_ID", -1)

                if (storeId != -1) {
                    isLoadingProducts = true
                    productErrorMessage = null

                    val response = RetrofitClient.instance.getProductsByStore(storeId)

                    if (response.code() == 401 || response.code() == 403) {
                        cerrarSesionLocal()
                        productErrorMessage = getApplication<Application>().getString(R.string.error_session_expired)
                        return@launch
                    }

                    if (response.isSuccessful) {
                        productList = response.body() ?: emptyList()
                    } else {
                        productErrorMessage = getApplication<Application>().getString(R.string.error_products_load_failed, response.code())
                    }
                }
            } catch (e: Exception) {
                productErrorMessage = getApplication<Application>().getString(R.string.error_network_products_load, e.localizedMessage)
            } finally {
                isLoadingProducts = false
            }
        }
    }

    fun cerrarSesionLocal(onLoggedOut: (() -> Unit)? = null) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("JWT_TOKEN")
            remove("USER_ID")
            remove("STORE_ID")
            apply()
        }
        RetrofitClient.context = null
        onLoggedOut?.invoke()
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
}
