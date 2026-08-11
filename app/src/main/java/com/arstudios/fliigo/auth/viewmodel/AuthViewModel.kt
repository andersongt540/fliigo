package com.arstudios.fliigo.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.auth.data.LoginRequest
import com.arstudios.fliigo.auth.data.RegistroInitRequest
import com.arstudios.fliigo.core.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    var currentView by mutableStateOf("login")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var passwordVisible by mutableStateOf(false)
    var mensajeEstado by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set

    fun login(context: Context, onLoginSuccess: (hasStore: Boolean) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            mensajeEstado = "Por favor llena todos los campos"
            Log.w(TAG, "Intento de login con campos vacíos")
            return
        }

        viewModelScope.launch {
            isLoading = true
            mensajeEstado = "Iniciando sesión..."
            Log.d(TAG, "Iniciando proceso de login para: $email")
            try {
                RetrofitClient.context = context.applicationContext

                val request = LoginRequest(email = email, password = password)
                val response = RetrofitClient.instance.loginUser(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token
                    val hasStore = body?.hasStore ?: false
                    val userId = body?.userId ?: -1
                    val storeId = body?.storeId ?: -1

                    if (!token.isNullOrEmpty() && userId != -1) {
                        val prefs = context.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("JWT_TOKEN", token)
                            putInt("USER_ID", userId)
                            putInt("STORE_ID", storeId)
                            apply()
                        }
                        mensajeEstado = "¡Bienvenido!"
                        Log.d(TAG, "Login exitoso. UserId: $userId, StoreId: $storeId, HasStore: $hasStore")
                        onLoginSuccess(hasStore)
                    } else {
                        mensajeEstado = "Error: Faltan datos en la respuesta"
                        Log.e(TAG, "Login fallido: Faltan datos esenciales en el body de la respuesta")
                    }
                } else {
                    val errorBodyStr = response.errorBody()?.string()
                    mensajeEstado = if (!errorBodyStr.isNullOrBlank()) {
                        try {
                            val jsonObj = JSONObject(errorBodyStr)
                            jsonObj.optString("error", errorBodyStr)
                        } catch (e: Exception) {
                            errorBodyStr
                        }
                    } else {
                        "Credenciales incorrectas o cuenta inactiva"
                    }
                    Log.e(TAG, "Error en respuesta HTTP login: ${response.code()} - $errorBodyStr")
                }
            } catch (e: Exception) {
                mensajeEstado = "Fallo de red: ${e.localizedMessage}"
                Log.e(TAG, "Excepción durante el login: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun register(context: Context) {
        if (email.isBlank() || password.isBlank()) {
            mensajeEstado = "Llena todos los campos"
            Log.w(TAG, "Intento de registro con campos vacíos")
            return
        }

        viewModelScope.launch {
            isLoading = true
            mensajeEstado = "Registrando cuenta..."
            Log.d(TAG, "Iniciando registro de cuenta para: $email")
            try {
                RetrofitClient.context = context.applicationContext
                val request = RegistroInitRequest(email = email, password = password)
                val response = RetrofitClient.instance.iniciarRegistro(request)

                if (response.isSuccessful) {
                    mensajeEstado = "¡Registro exitoso! Cuenta inactiva hasta aprobación."
                    Log.d(TAG, "Registro de cuenta exitoso para: $email")
                    currentView = "login"
                } else {
                    val errorBodyStr = response.errorBody()?.string()
                    mensajeEstado = if (!errorBodyStr.isNullOrBlank()) {
                        try {
                            val jsonObj = JSONObject(errorBodyStr)
                            jsonObj.optString("error", errorBodyStr)
                        } catch (e: Exception) {
                            errorBodyStr
                        }
                    } else {
                        "Error al registrar cuenta"
                    }
                    Log.e(TAG, "Error HTTP en registro: ${response.code()} - $errorBodyStr")
                }
            } catch (e: Exception) {
                mensajeEstado = "Fallo de red: ${e.localizedMessage}"
                Log.e(TAG, "Excepción durante el registro: ${e.localizedMessage}", e)
            } finally {
                isLoading = false
            }
        }
    }
}