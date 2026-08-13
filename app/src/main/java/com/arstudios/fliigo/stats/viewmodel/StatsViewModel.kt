package com.arstudios.fliigo.stats.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.core.network.RetrofitClient
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    var totalSalesMonth by mutableDoubleStateOf(0.0)
    var grossUtility by mutableDoubleStateOf(0.0)
    var isLoading by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadStats()
    }

    fun loadStats(refreshing: Boolean = false) {
        viewModelScope.launch {
            if (refreshing) isRefreshing = true else isLoading = true
            errorMessage = null
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId != -1) {
                    val response = RetrofitClient.instance.getStoreStats(storeId)
                    if (response.isSuccessful && response.body() != null) {
                        totalSalesMonth = response.body()!!.totalSalesMonth
                        grossUtility = response.body()!!.grossUtility
                    } else {
                        errorMessage = "Error al cargar estadísticas"
                    }
                } else {
                    errorMessage = "Tienda no identificada"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.localizedMessage}"
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }
}
