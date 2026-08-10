package com.arstudios.fliigo.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.data.model.LoginRequest
import com.arstudios.fliigo.data.model.RegistroInitRequest
import com.arstudios.fliigo.data.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel : ViewModel() {
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
            return
        }

        viewModelScope.launch {
            isLoading = true
            mensajeEstado = "Iniciando sesión..."
            try {
                RetrofitClient.context = context.applicationContext

                val request = LoginRequest(email = email, password = password)
                val response = RetrofitClient.instance.loginUser(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token
                    val hasStore = body?.hasStore ?: false
                    val userId = body?.userId ?: -1

                    if (!token.isNullOrEmpty() && userId != -1) {
                        val prefs = context.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("JWT_TOKEN", token)
                            putInt("USER_ID", userId)
                            apply()
                        }
                        mensajeEstado = "¡Bienvenido!"
                        onLoginSuccess(hasStore)
                    } else {
                        mensajeEstado = "Error: Faltan datos en la respuesta"
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
                }
            } catch (e: Exception) {
                mensajeEstado = "Fallo de red: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(context: Context) {
        if (email.isBlank() || password.isBlank()) {
            mensajeEstado = "Llena todos los campos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            mensajeEstado = "Registrando cuenta..."
            try {
                RetrofitClient.context = context.applicationContext
                val request = RegistroInitRequest(email = email, password = password)
                val response = RetrofitClient.instance.iniciarRegistro(request)

                if (response.isSuccessful) {
                    mensajeEstado = "¡Registro exitoso! Cuenta inactiva hasta aprobación."
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
                }
            } catch (e: Exception) {
                mensajeEstado = "Fallo de red: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}