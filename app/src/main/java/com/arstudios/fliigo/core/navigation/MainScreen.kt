package com.arstudios.fliigo.core.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.ui.screens.BalanceScreen
import com.arstudios.fliigo.debt.ui.DebtScreen
import com.arstudios.fliigo.enDesarrollo.UnderDevelopmentScreen
import com.arstudios.fliigo.inventory.ui.screens.InventoryScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val backToExitMessage = stringResource(R.string.toast_back_to_exit)

    // Manejo del botón de retroceder/gesto
    BackHandler(enabled = true) {
        if (showLogoutDialog) {
            showLogoutDialog = false
        } else if (selectedIndex != 0) {
            selectedIndex = 0
            lastBackPressTime = 0L // Reiniciar el contador al volver al balance
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                showLogoutDialog = true
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(context, backToExitMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val fondoVerde = colorResource(R.color.fondo_verde)
    val amarilloHeader = colorResource(R.color.amarillo_header)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = fondoVerde, contentColor = Color.White) {
                val navItems = listOf(
                    Triple(stringResource(R.string.nav_balance), "🧮", 0),
                    Triple(stringResource(R.string.nav_debt), "💼", 1),
                    Triple(stringResource(R.string.nav_stats), "📊", 2),
                    Triple(stringResource(R.string.nav_inventory), "📋", 3),
                )

                navItems.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Text(text = icon, fontSize = 20.sp) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = amarilloHeader.copy(alpha = 0.2f),
                            selectedIconColor = amarilloHeader,
                            unselectedIconColor = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Centralización de la navegación por índices
        when (selectedIndex) {
            0 -> BalanceScreen(Modifier.padding(innerPadding))
            1 -> DebtScreen(Modifier.padding(innerPadding))
            2 -> UnderDevelopmentScreen(Modifier.padding(innerPadding))
            3 -> InventoryScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_logout_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.dialog_logout_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        logout(context)
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_logout), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        stringResource(R.string.btn_cancel),
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

private fun logout(context: Context) {
    val prefs = context.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}
