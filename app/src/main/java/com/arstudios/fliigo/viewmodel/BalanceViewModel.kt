package com.arstudios.fliigo.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.data.model.ProductDto
import com.arstudios.fliigo.data.model.SaleItem
import com.arstudios.fliigo.data.model.SaleRequest
import com.arstudios.fliigo.data.network.RetrofitClient
import kotlinx.coroutines.launch

// Estados visuales para la UI de Balance
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

    var uiState: BalanceUiState by mutableStateOf(BalanceUiState.Loading)
        private set

    var productList by mutableStateOf<List<ProductDto>>(emptyList())
        private set

    var isLoadingProducts by mutableStateOf(false)
        private set

    // Estado para exponer el error de productos en la UI
    var productErrorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadStoreBalance()
    }

    fun loadStoreBalance() {
        viewModelScope.launch {
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getInt("USER_ID", -1)

                if (userId == -1) {
                    uiState = BalanceUiState.Error("Usuario no identificado.")
                    return@launch
                }

                val response = RetrofitClient.instance.getStoreBalance(userId)

                // Si el servidor indica no autorizado o prohibido (usuario borrado / inactivo)
                if (response.code() == 401 || response.code() == 403) {
                    cerrarSesionLocal()
                    uiState = BalanceUiState.Error("La sesión ha expirado o el usuario ya no existe.")
                    return@launch
                }

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // Mapeo correcto de los DTOs de ventas a la UI
                    val mappedSales = data.sales?.map { dto ->
                        SaleItem(
                            productName = dto.productName ?: "Sin producto",
                            clientName = dto.clientName ?: "Cliente general",
                            amount = dto.amount ?: 0.0
                        )
                    } ?: emptyList()

                    uiState = BalanceUiState.Success(
                        storeName = data.storeName ?: "Tienda AR",
                        category = data.category ?: "",
                        balance = data.balance ?: 0.0,
                        totalIncome = data.totalIncome ?: 0.0,
                        totalExpenses = data.totalExpenses ?: 0.0,
                        salesList = mappedSales
                    )
                } else {
                    uiState = BalanceUiState.Error("No se pudo cargar el balance desde el servidor.")
                }
            } catch (e: Exception) {
                uiState = BalanceUiState.Error("Error de red: ${e.localizedMessage}")
            }
        }
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
                    onError("Error: Tienda no identificada.")
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
                    onError("Sesión expirada. Por favor, inicia sesión nuevamente.")
                    return@launch
                }

                if (response.isSuccessful) {
                    onSuccess()
                    loadStoreBalance()
                } else {
                    onError("No se pudo registrar la venta en el servidor.")
                }

            } catch (e: Exception) {
                onError("Error de red al registrar venta: ${e.localizedMessage}")
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
                    productErrorMessage = null // Limpiamos errores previos antes de intentar

                    val response = RetrofitClient.instance.getProductsByStore(storeId)

                    if (response.code() == 401 || response.code() == 403) {
                        cerrarSesionLocal()
                        productErrorMessage = "Sesión expirada o usuario inactivo."
                        return@launch
                    }

                    if (response.isSuccessful) {
                        productList = response.body() ?: emptyList()
                    } else {
                        val errorMsg = "Error del servidor al obtener productos: ${response.code()}"
                        productErrorMessage = errorMsg
                        Log.e("BalanceViewModel", errorMsg)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Excepción de red al cargar productos: ${e.localizedMessage}"
                productErrorMessage = errorMsg
                Log.e("BalanceViewModel", errorMsg, e)
            } finally {
                isLoadingProducts = false
            }
        }
    }

    fun cerrarSesionLocal(onLoggedOut: (() -> Unit)? = null) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .remove("JWT_TOKEN")
            .remove("USER_ID")
            .remove("STORE_ID")
            .apply()

        RetrofitClient.context = null
        onLoggedOut?.invoke()
    }
}