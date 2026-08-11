package com.arstudios.fliigo.core.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://backend-tienda-app-en-vps-1.onrender.com/"

    // Referencia al contexto para leer o limpiar las credenciales de SharedPreferences
    var context: Context? = null

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()

            // Obtener el token guardado localmente
            val token = context?.let { ctx ->
                val prefs = ctx.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                prefs.getString("JWT_TOKEN", null)
            }

            val requestBuilder = originalRequest.newBuilder()
            // Si el token existe, se añade automáticamente a la cabecera
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            // Ejecutar la petición
            val response = chain.proceed(requestBuilder.build())

            // Validación: Si el backend responde 401 (No autorizado) o 403 (Prohibido / Usuario borrado o inactivo)
            if (response.code == 401 || response.code == 403) {
                context?.let { ctx ->
                    val prefs = ctx.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                    // Borramos toda la sesión local automáticamente para forzar el retorno al login
                    prefs.edit()
                        .remove("JWT_TOKEN")
                        .remove("USER_ID")
                        .remove("STORE_ID")
                        .apply()
                }
            }

            response
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}