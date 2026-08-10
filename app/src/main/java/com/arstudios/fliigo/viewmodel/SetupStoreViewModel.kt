package com.arstudios.fliigo.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.data.network.RetrofitClient
import com.arstudios.fliigo.data.model.StoreSetupRequest
import kotlinx.coroutines.launch

class SetupStoreViewModel : ViewModel() {
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
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Recuperar el USER_ID que guardamos en el login
                val prefs = context.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("USER_ID", -1)

                if (userId == -1) {
                    errorMessage = "Sesión no encontrada. Inicia sesión nuevamente."
                    isLoading = false
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

                    // Guardar el STORE_ID en SharedPreferences para que InventoryViewModel y BalanceViewModel lo usen
                    responseBody?.let {
                        // Asegúrate de que tu modelo UsuarioResponse tenga la propiedad storeId (o cámbiala por el nombre exacto de tu variable)
                        val storeIdValue = it.storeId ?: -1
                        prefs.edit().putInt("STORE_ID", storeIdValue).apply()
                    }

                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = if (!errorBody.isNullOrBlank()) "Error: $errorBody" else "Error al registrar la tienda"
                }
            } catch (e: Exception) {
                errorMessage = "Fallo de red: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}