package com.arstudios.fliigo.SetupStore.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.core.network.RetrofitClient
import com.arstudios.fliigo.SetupStore.data.StoreSetupRequest
import kotlinx.coroutines.launch

class SetupStoreViewModel : ViewModel() {
    companion object {
        private const val TAG = "SetupStoreViewModel"
    }

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun registerStore(
        context: Context,
        storeName: String,
        category: String,
        address: String,
        phone: String,
        onSuccess: () -> Unit
    ) {
        if (storeName.isBlank() || category.isBlank() || address.isBlank() || phone.isBlank()) {
            errorMessage = "Por favor llena todos los campos"
            Log.w(TAG, "Intento de registro de tienda con campos incompletos.")
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            Log.d(TAG, "Registrando tienda: $storeName")
            try {
                val prefs = context.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("USER_ID", -1)

                if (userId == -1) {
                    errorMessage = "Sesión no encontrada. Inicia sesión nuevamente."
                    isLoading = false
                    Log.e(TAG, "Error: UserId no encontrado al configurar tienda.")
                    return@launch
                }

                RetrofitClient.context = context.applicationContext
                val request = StoreSetupRequest(
                    userId = userId,
                    storeName = storeName,
                    category = category,
                    address = address,
                    phone = phone
                )

                val response = RetrofitClient.instance.setupStore(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val storeIdValue = it.storeId ?: -1
                        prefs.edit().putInt("STORE_ID", storeIdValue).apply()
                        Log.d(TAG, "Tienda configurada exitosamente. StoreId asignado: $storeIdValue")
                    }
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = if (!errorBody.isNullOrBlank()) "Error: $errorBody" else "Error al registrar la tienda"
                    Log.e(TAG, "Error HTTP al configurar tienda: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                errorMessage = "Fallo de red: ${e.localizedMessage}"
                Log.e(TAG, "Excepción de red al configurar tienda: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }
}