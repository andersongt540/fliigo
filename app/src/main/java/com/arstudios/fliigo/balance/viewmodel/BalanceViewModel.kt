// app/src/main/java/com/arstudios/fliigo/viewmodel/BalanceViewModel.kt
package com.arstudios.fliigo.balance.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.inventory.data.ProductDto
import com.arstudios.fliigo.balance.data.SaleItem
import com.arstudios.fliigo.balance.data.SaleRequest
import com.arstudios.fliigo.core.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    init {
        Log.d(TAG, "Inicializando BalanceViewModel. Llamando a loadStoreBalance()...")
        loadStoreBalance()
    }

    fun loadStoreBalance() {
        viewModelScope.launch {
            Log.d(TAG, "=== INICIO loadStoreBalance ===")
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getInt("USER_ID", -1)
                Log.d(TAG, "USER_ID recuperado de SharedPreferences: $userId")

                if (userId == -1) {
                    uiState = BalanceUiState.Error("Usuario no identificado.")
                    Log.e(TAG, "Error: UserId no encontrado en SharedPreferences.")
                    return@launch
                }

                Log.d(TAG, "Realizando llamada Retrofit getStoreBalance con userId: $userId")
                val response = RetrofitClient.instance.getStoreBalance(userId)
                Log.d(TAG, "Respuesta recibida de getStoreBalance. Código HTTP: ${response.code()}")

                if (response.code() == 401 || response.code() == 403) {
                    Log.w(TAG, "Sesión no autorizada o prohibida (${response.code()}). Cerrando sesión local...")
                    cerrarSesionLocal()
                    uiState = BalanceUiState.Error("La sesión ha expirado o el usuario ya no existe.")
                    return@launch
                }

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    Log.d(TAG, "Cuerpo de la respuesta getStoreBalance exitoso. Mapeando ventas...")
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
                    Log.d(TAG, "Balance cargado exitosamente para la tienda: ${data.storeName}. Total de ventas mapeadas: ${mappedSales.size}")
                } else {
                    uiState = BalanceUiState.Error("No se pudo cargar el balance desde el servidor.")
                    val errorBodyStr = response.errorBody()?.string() ?: "Sin detalles"
                    Log.e(TAG, "Error HTTP al cargar balance. Código: ${response.code()}, ErrorBody: $errorBodyStr")
                }
            } catch (e: Exception) {
                uiState = BalanceUiState.Error("Error de red: ${e.localizedMessage}")
                Log.e(TAG, "Excepción crítica al cargar balance: ${e.localizedMessage}", e)
            } finally {
                Log.d(TAG, "=== FIN loadStoreBalance ===")
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
            Log.d(TAG, "=== INICIO registerSale ===")
            Log.d(TAG, "Parámetros de venta -> Producto ID: $productId, Cantidad: $quantity, Cliente: $clientName, Tel: $phone, Dir: $address")
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = sharedPreferences.getInt("STORE_ID", -1)
                Log.d(TAG, "STORE_ID recuperado de SharedPreferences: $storeId")

                if (storeId == -1) {
                    onError("Error: Tienda no identificada.")
                    Log.e(TAG, "Error al registrar venta: StoreId no encontrado en SharedPreferences.")
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

                Log.d(TAG, "Enviando solicitud registerSale al servidor...")
                val response = RetrofitClient.instance.registerSale(request)
                Log.d(TAG, "Respuesta de registerSale recibida. Código HTTP: ${response.code()}")

                if (response.code() == 401 || response.code() == 403) {
                    Log.w(TAG, "Sesión expirada durante el registro de venta (${response.code()}). Cerrando sesión local...")
                    cerrarSesionLocal()
                    onError("Sesión expirada. Por favor, inicia sesión nuevamente.")
                    return@launch
                }

                if (response.isSuccessful) {
                    Log.d(TAG, "Venta registrada exitosamente en el servidor.")

                    // Ejecutamos callback para cerrar UI/diálogos
                    onSuccess()

                    // Pausa de seguridad para asegurar consistencia en base de datos remota
                    Log.d(TAG, "Esperando 500ms antes de recargar el balance...")
                    delay(500)

                    Log.d(TAG, "Actualizando balance de la tienda tras registrar la venta...")
                    loadStoreBalance()
                } else {
                    val errorBodyStr = response.errorBody()?.string() ?: "Sin detalles"
                    onError("No se pudo registrar la venta en el servidor.")
                    Log.e(TAG, "Error HTTP al registrar venta. Código: ${response.code()}, Detalles: $errorBodyStr")
                }

            } catch (e: Exception) {
                onError("Error de red al registrar venta: ${e.localizedMessage}")
                Log.e(TAG, "Excepción crítica al registrar venta: ${e.localizedMessage}", e)
            } finally {
                Log.d(TAG, "=== FIN registerSale ===")
            }
        }
    }

    fun loadStoreProducts() {
        viewModelScope.launch {
            Log.d(TAG, "=== INICIO loadStoreProducts ===")
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = sharedPreferences.getInt("STORE_ID", -1)
                Log.d(TAG, "STORE_ID recuperado para cargar productos: $storeId")

                if (storeId != -1) {
                    isLoadingProducts = true
                    productErrorMessage = null

                    Log.d(TAG, "Realizando llamada Retrofit getProductsByStore con storeId: $storeId")
                    val response = RetrofitClient.instance.getProductsByStore(storeId)
                    Log.d(TAG, "Respuesta recibida de getProductsByStore. Código HTTP: ${response.code()}")

                    if (response.code() == 401 || response.code() == 403) {
                        Log.w(TAG, "Sesión expirada al cargar productos (${response.code()}). Cerrando sesión local...")
                        cerrarSesionLocal()
                        productErrorMessage = "Sesión expirada o usuario inactivo."
                        return@launch
                    }

                    if (response.isSuccessful) {
                        productList = response.body() ?: emptyList()
                        Log.d(TAG, "Productos cargados correctamente. Total en lista: ${productList.size}")
                    } else {
                        val errorBodyStr = response.errorBody()?.string() ?: "Sin detalles"
                        val errorMsg = "Error del servidor al obtener productos: ${response.code()}"
                        productErrorMessage = errorMsg
                        Log.e(TAG, "$errorMsg - Detalles: $errorBodyStr")
                    }
                } else {
                    Log.w(TAG, "Aviso: STORE_ID es -1, no se pueden cargar los productos.")
                }
            } catch (e: Exception) {
                val errorMsg = "Excepción de red al cargar productos: ${e.localizedMessage}"
                productErrorMessage = errorMsg
                Log.e(TAG, errorMsg, e)
            } finally {
                isLoadingProducts = false
                Log.d(TAG, "=== FIN loadStoreProducts ===")
            }
        }
    }

    fun cerrarSesionLocal(onLoggedOut: (() -> Unit)? = null) {
        Log.i(TAG, "=== INICIO cerrarSesionLocal ===")
        val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.remove("JWT_TOKEN")
        editor.remove("USER_ID")
        editor.remove("STORE_ID")
        editor.apply()
        Log.d(TAG, "Credenciales eliminadas de SharedPreferences (JWT_TOKEN, USER_ID, STORE_ID).")

        RetrofitClient.context = null
        Log.i(TAG, "Sesión local limpiada correctamente. RetrofitClient.context reiniciado a null.")

        onLoggedOut?.invoke()
        Log.i(TAG, "=== FIN cerrarSesionLocal ===")
    }
}