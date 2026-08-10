package com.arstudios.fliigo.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.data.model.CategoryDto
import com.arstudios.fliigo.data.model.ProductDto
import com.arstudios.fliigo.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la interfaz de inventario conectado a la BD
data class InventoryUiState(
    val products: List<ProductDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventoryData()
    }

    // Método para cargar productos y categorías desde el backend usando el STORE_ID
    fun loadInventoryData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Tienda no identificada.") }
                    return@launch
                }

                // Peticiones al backend definidas en ApiService
                val productsResponse = RetrofitClient.instance.getProductsByStore(storeId)
                val categoriesResponse = RetrofitClient.instance.getCategoriesByStore(storeId)

                if (productsResponse.isSuccessful && categoriesResponse.isSuccessful) {
                    val products = productsResponse.body() ?: emptyList()
                    val categories = categoriesResponse.body() ?: emptyList()

                    _uiState.update {
                        it.copy(
                            products = products,
                            categories = categories,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar los datos del inventario desde el servidor."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de red: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // Método para crear una nueva categoría en el backend
    fun createCategory(categoryName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) return@launch

                val requestMap = mapOf(
                    "storeId" to storeId,
                    "name" to categoryName
                )

                val response = RetrofitClient.instance.createCategory(requestMap)
                if (response.isSuccessful) {
                    loadInventoryData() // Recargar datos tras crear
                    onComplete()
                } else {
                    _uiState.update { it.copy(errorMessage = "No se pudo registrar la categoría.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red: ${e.localizedMessage}") }
            }
        }
    }

    // Método para registrar un nuevo producto en el backend
    fun registerProduct(
        name: String,
        price: Double,
        stock: Int,
        category: String?,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) return@launch

                val productDto = ProductDto(
                    storeId = storeId,
                    name = name,
                    price = price,
                    costPrice = 0.0,
                    provider = "",
                    stock = stock,
                    category = category
                )

                val response = RetrofitClient.instance.registerProduct(productDto)
                if (response.isSuccessful) {
                    loadInventoryData() // Recargar inventario tras registrar
                    onComplete()
                } else {
                    _uiState.update { it.copy(errorMessage = "No se pudo registrar el producto.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red: ${e.localizedMessage}") }
            }
        }
    }
}