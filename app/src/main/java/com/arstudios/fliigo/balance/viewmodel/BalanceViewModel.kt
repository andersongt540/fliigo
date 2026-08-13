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
import com.arstudios.fliigo.balance.data.GroupedSale
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
        val salesList: List<GroupedSale> = emptyList()
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
                    
                    val serverSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC") // El server guarda en UTC
                    }
                    val localSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val todayLocal = localSdf.format(Date())

                    Log.d(TAG, "Día local del teléfono: $todayLocal")

                    val mappedSales = data.sales?.map { dto ->
                        // Intentamos convertir la fecha del servidor (UTC) a la local del teléfono
                        val localDateString = try {
                            val dateObj = serverSdf.parse(dto.date ?: "")
                            if (dateObj != null) localSdf.format(dateObj) else ""
                        } catch (_: Exception) { "" }

                        SaleItem(
                            id = dto.id ?: 0,
                            productName = dto.productName ?: "Sin producto",
                            clientName = dto.clientName ?: "Cliente general",
                            amount = dto.amount ?: 0.0,
                            date = dto.date ?: "",
                            address = dto.address ?: "",
                            phone = dto.phone ?: "",
                            quantity = dto.quantity ?: 1,
                            localDay = localDateString // Campo auxiliar para filtrar
                        )
                    }?.filter { it.localDay == todayLocal } ?: emptyList()

                    // Agrupar ventas por fecha exacta y cliente
                    val groupedSales = mappedSales.groupBy { it.date + it.clientName }
                        .map { (key, items) ->
                            val first = items.first()
                            GroupedSale(
                                clientName = first.clientName,
                                date = first.date,
                                address = first.address,
                                phone = first.phone,
                                items = items,
                                totalAmount = items.sumOf { it.amount }
                            )
                        }

                    Log.d(TAG, "Ventas encontradas para hoy local: ${groupedSales.size} grupos")

                    uiState = BalanceUiState.Success(
                        storeName = data.storeName ?: "Tienda AR",
                        category = data.category ?: "",
                        balance = data.balance ?: 0.0,
                        totalIncome = data.totalIncome ?: 0.0,
                        totalExpenses = data.totalExpenses ?: 0.0,
                        salesList = groupedSales
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
        Log.d(TAG, "registerSaleWithValidation: client=$clientName, rows=${productRows.size}")
        val app = getApplication<Application>()
        if (clientName.isBlank()) {
            onError(app.getString(R.string.error_empty_client))
            return
        }

        if (productRows.isEmpty() || productRows.any { it.productId.isBlank() }) {
            onError(app.getString(R.string.error_no_products))
            return
        }

        val validatedItems = mutableListOf<com.arstudios.fliigo.balance.data.SaleItemRequest>()

        for (row in productRows) {
            val inputCode = row.productId.trim()
            val requestedQty = row.quantity.toIntOrNull() ?: 0
            Log.d(TAG, "Validando fila: inputCode=$inputCode, qty=$requestedQty")

            if (inputCode.isBlank() || requestedQty <= 0) {
                onError(app.getString(R.string.error_invalid_prod_qty))
                return
            }

            // Buscar por barcode primero, luego por ID
            var matchedProduct = productList.find { it.barcode == inputCode }
            if (matchedProduct == null) {
                Log.d(TAG, "No se encontró por barcode, intentando por ID...")
                val pIdInt = inputCode.toIntOrNull()
                if (pIdInt != null) {
                    matchedProduct = productList.find { it.id == pIdInt }
                }
            }

            if (matchedProduct == null) {
                Log.e(TAG, "Producto no encontrado para el código: $inputCode")
                onError(app.getString(R.string.error_product_not_found))
                return
            }

            Log.d(TAG, "Producto encontrado: ${matchedProduct.name}, stock actual=${matchedProduct.stock}")

            if (matchedProduct.stock < requestedQty) {
                Log.e(TAG, "Stock insuficiente para ${matchedProduct.name}")
                onError(app.getString(R.string.error_insufficient_stock, matchedProduct.name, matchedProduct.stock))
                return
            }
            
            matchedProduct.id?.let { 
                validatedItems.add(com.arstudios.fliigo.balance.data.SaleItemRequest(it, requestedQty))
            }
        }

        if (validatedItems.isEmpty()) return

        Log.d(TAG, "Validación exitosa (${validatedItems.size} ítems). Procediendo a registrar venta...")
        registerSale(
            clientName = clientName,
            address = address,
            phone = phone,
            items = validatedItems,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun registerSale(
        clientName: String,
        address: String,
        phone: String,
        items: List<com.arstudios.fliigo.balance.data.SaleItemRequest>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "registerSale: items=${items.size}")
        viewModelScope.launch {
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = sharedPreferences.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    Log.e(TAG, "Error: storeId no encontrado")
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

                Log.d(TAG, "Enviando SaleRequest: $request")
                val response = RetrofitClient.instance.registerSale(request)
                Log.d(TAG, "registerSale: Respuesta cod=${response.code()} isSuccessful=${response.isSuccessful}")

                if (response.code() == 401 || response.code() == 403) {
                    cerrarSesionLocal()
                    onError(getApplication<Application>().getString(R.string.error_session_expired))
                    return@launch
                }

                if (response.isSuccessful) {
                    Log.d(TAG, "Venta registrada con éxito")
                    onSuccess()
                    delay(500)
                    loadStoreBalance()
                    loadStoreProducts()
                } else {
                    Log.e(TAG, "Error al registrar venta: ${response.errorBody()?.string()}")
                    onError(getApplication<Application>().getString(R.string.error_register_sale_failed))
                }

            } catch (e: Exception) {
                Log.e(TAG, "registerSale EXCEPTION: ${e.localizedMessage}")
                onError(getApplication<Application>().getString(R.string.error_network_register_sale, e.localizedMessage))
            }
        }
    }

    fun loadStoreProducts() {
        Log.d(TAG, "loadStoreProducts: Cargando lista de productos...")
        viewModelScope.launch {
            try {
                val sharedPreferences = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = sharedPreferences.getInt("STORE_ID", -1)

                if (storeId != -1) {
                    isLoadingProducts = true
                    productErrorMessage = null

                    val response = RetrofitClient.instance.getProductsByStore(storeId)
                    Log.d(TAG, "loadStoreProducts: Respuesta cod=${response.code()}")

                    if (response.code() == 401 || response.code() == 403) {
                        cerrarSesionLocal()
                        productErrorMessage = getApplication<Application>().getString(R.string.error_session_expired)
                        return@launch
                    }

                    if (response.isSuccessful) {
                        productList = response.body() ?: emptyList()
                        Log.d(TAG, "loadStoreProducts: ${productList.size} productos cargados")
                    } else {
                        Log.e(TAG, "Error al cargar productos: ${response.errorBody()?.string()}")
                        productErrorMessage = getApplication<Application>().getString(R.string.error_products_load_failed, response.code())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadStoreProducts EXCEPTION: ${e.localizedMessage}")
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

    fun deleteGroupedSale(groupedSale: GroupedSale, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                var allSuccess = true
                var lastError = ""
                
                // Eliminamos cada ítem de la venta agrupada
                groupedSale.items.forEach { item ->
                    val response = RetrofitClient.instance.deleteSale(item.id)
                    if (!response.isSuccessful) {
                        allSuccess = false
                        lastError = "Error al eliminar ${item.productName}"
                    }
                }

                if (allSuccess) {
                    onSuccess()
                    loadStoreBalance()
                } else {
                    onError(lastError)
                }
            } catch (e: Exception) {
                onError("Error de red: ${e.localizedMessage}")
            }
        }
    }
}
