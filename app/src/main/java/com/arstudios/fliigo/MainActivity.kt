package com.arstudios.fliigo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arstudios.fliigo.data.network.RetrofitClient
import com.arstudios.fliigo.ui.navigation.MainScreen
import com.arstudios.fliigo.ui.screens.AuthScreen
import com.arstudios.fliigo.ui.screens.SetupStoreScreen
import com.arstudios.fliigo.ui.theme.FliigoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        RetrofitClient.context = applicationContext

        setContent {
            FliigoTheme {
                val sharedPreferences = getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)

                // Verificación inicial robusta: Token presente y IDs válidos
                val savedToken = sharedPreferences.getString("JWT_TOKEN", null)
                val userId = sharedPreferences.getInt("USER_ID", -1)
                val storeId = sharedPreferences.getInt("STORE_ID", -1)

                val isSessionValid = !savedToken.isNullOrEmpty() && userId != -1 && storeId != -1

                var currentScreen by remember {
                    mutableStateOf(if (isSessionValid) "main" else "auth")
                }

                // Escuchar cambios en SharedPreferences para forzar el logout si se borran las credenciales
                DisposableEffect(sharedPreferences) {
                    val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                        if (key == "JWT_TOKEN" && prefs.getString("JWT_TOKEN", null).isNullOrEmpty()) {
                            currentScreen = "auth"
                        }
                    }
                    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                    onDispose {
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "auth" -> {
                            AuthScreen(
                                onLoginSuccess = { hasStore ->
                                    currentScreen = if (hasStore) "main" else "setup"
                                }
                            )
                        }
                        "setup" -> {
                            SetupStoreScreen(
                                onStoreCreated = {
                                    currentScreen = "main"
                                }
                            )
                        }
                        "main" -> {
                            MainAppContent(modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        MainScreen()
    }
}