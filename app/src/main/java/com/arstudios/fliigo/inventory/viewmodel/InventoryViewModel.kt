// app/src/main/java/com/arstudios/fliigo/inventory/viewmodel/InventoryViewModel.kt
package com.arstudios.fliigo.inventory.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.inventory.data.CategoryDto
import com.arstudios.fliigo.inventory.data.ProductDto
import com.arstudios.fliigo.core.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InventoryUiState(
    val products: List<ProductDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "InventoryViewModel"
    }

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadInventoryData()
    }

    fun loadInventoryData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            Log.d(TAG, "Cargando datos de inventario...")
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Tienda no identificada. Por favor, inicia sesión nuevamente."
                        )
                    }
                    Log.e(TAG, "Error: StoreId no encontrado en SharedPreferences (-1).")
                    return@launch
                }

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
                    Log.d(TAG, "Inventario cargado exitosamente. Productos: ${products.size}, Categorías: ${categories.size}")
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar los datos del inventario desde el servidor."
                        )
                    }
                    Log.e(TAG, "Error HTTP inventario - Productos: ${productsResponse.code()}, Categorías: ${categoriesResponse.code()}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error de red: ${e.localizedMessage}"
                    )
                }
                Log.e(TAG, "Excepción al cargar inventario: ${e.localizedMessage}", e)
            }
        }
    }

    fun createCategory(categoryName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            Log.d(TAG, "Creando categoría: $categoryName")
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    Log.e(TAG, "Error al crear categoría: StoreId no válido.")
                    return@launch
                }

                val requestMap = mapOf(
                    "storeId" to storeId as Any,
                    "name" to categoryName as Any
                )

                val response = RetrofitClient.instance.createCategory(requestMap)
                if (response.isSuccessful) {
                    Log.d(TAG, "Categoría creada con éxito.")
                    loadInventoryData()
                    onComplete()
                } else {
                    _uiState.update { it.copy(errorMessage = "No se pudo registrar la categoría.") }
                    Log.e(TAG, "Error HTTP al crear categoría: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red: ${e.localizedMessage}") }
                Log.e(TAG, "Excepción al crear categoría: ${e.localizedMessage}", e)
            }
        }
    }

    fun registerProduct(
        name: String,
        price: Double,
        costPrice: Double?,
        provider: String?,
        stock: Int,
        category: String?,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Registrando producto: $name")
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    _uiState.update { it.copy(errorMessage = "Tienda no identificada.") }
                    return@launch
                }

                val productDto = ProductDto(
                    storeId = storeId,
                    name = name,
                    price = price,
                    costPrice = costPrice ?: 0.0,
                    provider = provider ?: "",
                    stock = stock,
                    category = category
                )

                val response = RetrofitClient.instance.registerProduct(productDto)
                if (response.isSuccessful) {
                    Log.d(TAG, "Producto registrado con éxito.")
                    loadInventoryData()
                    onComplete()
                } else {
                    _uiState.update { it.copy(errorMessage = "No se pudo registrar el producto.") }
                    Log.e(TAG, "Error HTTP al registrar producto: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red: ${e.localizedMessage}") }
                Log.e(TAG, "Excepción al registrar producto: ${e.localizedMessage}", e)
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "Eliminando producto con ID: $productId")
            try {
                val response = RetrofitClient.instance.deleteProduct(productId)
                if (response.isSuccessful) {
                    Log.d(TAG, "Producto eliminado con éxito.")
                    loadInventoryData()
                } else {
                    _uiState.update { it.copy(errorMessage = "No se pudo eliminar el producto.") }
                    Log.e(TAG, "Error HTTP al eliminar producto: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red: ${e.localizedMessage}") }
                Log.e(TAG, "Excepción al eliminar producto: ${e.localizedMessage}", e)
            }
        }
    }

    fun updateProduct(
        productId: Int,
        name: String,
        price: Double,
        costPrice: Double?,
        provider: String?,
        stock: Int,
        category: String?,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Actualizando producto ID: $productId")
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    _uiState.update { it.copy(errorMessage = "Tienda no identificada.") }
                    return@launch
                }

                val productDto = ProductDto(
                    id = productId,
                    storeId = storeId,
                    name = name,
                    price = price,
                    costPrice = costPrice ?: 0.0,
                    provider = provider ?: "",
                    stock = stock,
                    category = category
                )

                val response = RetrofitClient.instance.updateProduct(productId, productDto)
                if (response.isSuccessful) {
                    Log.d(TAG, "Producto actualizado con éxito.")
                    loadInventoryData()
                    onComplete()
                } else {
                    _uiState.update { it.copy(errorMessage = "No se pudo actualizar el producto.") }
                    Log.e(TAG, "Error HTTP al actualizar producto: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error de red: ${e.localizedMessage}") }
                Log.e(TAG, "Excepción al actualizar producto: ${e.localizedMessage}", e)
            }
        }
    }
}