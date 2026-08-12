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
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) return@launch

                val productsResponse = RetrofitClient.instance.getProductsByStore(storeId)
                val categoriesResponse = RetrofitClient.instance.getCategoriesByStore(storeId)

                if (productsResponse.isSuccessful && categoriesResponse.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            products = productsResponse.body() ?: emptyList(),
                            categories = categoriesResponse.body() ?: emptyList(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    fun createCategory(categoryName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val storeId = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE).getInt("STORE_ID", -1)
                val requestMap = mapOf("storeId" to storeId, "name" to categoryName)
                val response = RetrofitClient.instance.createCategory(requestMap)
                if (response.isSuccessful) {
                    loadInventoryData()
                    onComplete()
                }
            } catch (e: Exception) { /* Log error */ }
        }
    }

    fun registerProduct(
        name: String,
        price: Double,
        costPrice: Double?,
        provider: String?,
        stock: Int,
        category: String?,
        barcode: String? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val storeId = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE).getInt("STORE_ID", -1)
                val productDto = ProductDto(
                    storeId = storeId,
                    name = name,
                    price = price,
                    costPrice = costPrice ?: 0.0,
                    provider = provider ?: "",
                    stock = stock,
                    category = category,
                    barcode = barcode
                )
                val response = RetrofitClient.instance.registerProduct(productDto)
                if (response.isSuccessful) {
                    loadInventoryData()
                    onComplete()
                }
            } catch (e: Exception) { /* Log error */ }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteProduct(productId)
                if (response.isSuccessful) loadInventoryData()
            } catch (e: Exception) { /* Log error */ }
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
            try {
                val storeId = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE).getInt("STORE_ID", -1)
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
                    loadInventoryData()
                    onComplete()
                }
            } catch (e: Exception) { /* Log error */ }
        }
    }
}
