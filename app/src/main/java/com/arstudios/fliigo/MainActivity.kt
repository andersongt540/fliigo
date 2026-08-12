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
import com.arstudios.fliigo.SetupStore.ui.SetupStoreScreen
import com.arstudios.fliigo.SetupStore.ui.screens.ModuleSelectionScreen
import com.arstudios.fliigo.auth.ui.screens.AuthScreen
import com.arstudios.fliigo.core.navigation.MainScreen
import com.arstudios.fliigo.core.network.RetrofitClient
import com.arstudios.fliigo.core.theme.FliigoTheme

enum class AppScreen {
    Auth, SetupStore, ModuleSelection, Main
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.context = applicationContext
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemUI()

        setContent {
            FliigoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prefs = getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                    
                    var currentScreen by remember {
                        mutableStateOf(
                            if (prefs.getString("JWT_TOKEN", null) != null) {
                                if (prefs.getInt("STORE_ID", -1) != -1) {
                                    // Si ya tiene tienda, verificamos si ya configuró módulos
                                    if (prefs.contains("module_balance")) AppScreen.Main
                                    else AppScreen.ModuleSelection
                                } else {
                                    AppScreen.SetupStore
                                }
                            } else {
                                AppScreen.Auth
                            }
                        )
                    }

                    when (currentScreen) {
                        AppScreen.Auth -> {
                            AuthScreen(onLoginSuccess = { hasStore ->
                                currentScreen = if (hasStore) {
                                    if (prefs.contains("module_balance")) AppScreen.Main
                                    else AppScreen.ModuleSelection
                                } else {
                                    AppScreen.SetupStore
                                }
                            })
                        }
                        AppScreen.SetupStore -> {
                            SetupStoreScreen(onStoreCreated = {
                                currentScreen = AppScreen.ModuleSelection
                            })
                        }
                        AppScreen.ModuleSelection -> {
                            ModuleSelectionScreen(onFinish = {
                                currentScreen = AppScreen.Main
                            })
                        }
                        AppScreen.Main -> {
                            MainScreen(onLogout = { 
                                currentScreen = AppScreen.Auth 
                            })
                        }
                    }
                }
            }
        }
    }

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
