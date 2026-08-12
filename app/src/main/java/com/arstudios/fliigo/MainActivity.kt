package com.arstudios.fliigo

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.arstudios.fliigo.auth.ui.screens.AuthScreen
import com.arstudios.fliigo.core.navigation.MainScreen
import com.arstudios.fliigo.core.network.RetrofitClient
import com.arstudios.fliigo.core.theme.FliigoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el contexto de Retrofit inmediatamente
        RetrofitClient.context = applicationContext

        // 1. Permitir que el contenido dibuje detrás de las barras del sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Ocultar la barra de estado y la barra de navegación (Modo Inmersivo)
        hideSystemUI()

        setContent {
            FliigoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoggedIn by remember {
                        mutableStateOf(
                            getSharedPreferences("FliigoPrefs", MODE_PRIVATE)
                                .getString("JWT_TOKEN", null) != null
                        )
                    }

                    if (isLoggedIn) {
                        MainScreen(onLogout = { isLoggedIn = false })
                    } else {
                        AuthScreen(onLoginSuccess = { isLoggedIn = true })
                    }
                }
            }
        }
    }

    // Opcional: Asegurar que la pantalla completa se mantenga al volver a enfocar la app
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}