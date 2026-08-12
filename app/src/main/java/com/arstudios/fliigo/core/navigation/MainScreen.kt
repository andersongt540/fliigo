package com.arstudios.fliigo.core.navigation

import android.content.Context
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
import com.arstudios.fliigo.stats.ui.StatsScreen
import com.arstudios.fliigo.inventory.ui.screens.InventoryScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)

    // Leer módulos habilitados (Usamos stringResource fuera del remember si es posible, o pasamos el contexto)
    val balanceLabel = stringResource(R.string.nav_balance)
    val debtLabel = stringResource(R.string.nav_debt)
    val statsLabel = stringResource(R.string.nav_stats)
    val inventoryLabel = stringResource(R.string.nav_inventory)

    val enabledModules = remember(balanceLabel, debtLabel, statsLabel, inventoryLabel) {
        mutableListOf<Triple<String, String, @Composable (Modifier) -> Unit>>().apply {
            if (prefs.getBoolean("module_balance", true)) {
                add(Triple(balanceLabel, "🧮") { mod -> BalanceScreen(mod) })
            }
            if (prefs.getBoolean("module_debt", true)) {
                add(Triple(debtLabel, "💼") { mod -> DebtScreen(mod) })
            }
            if (prefs.getBoolean("module_stats", true)) {
                add(Triple(statsLabel, "📊") { mod -> StatsScreen(mod) })
            }
            if (prefs.getBoolean("module_inventory", true)) {
                add(Triple(inventoryLabel, "📋") { mod -> InventoryScreen(mod) })
            }
        }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val backToExitMessage = stringResource(R.string.toast_back_to_exit)

    BackHandler(enabled = true) {
        if (showLogoutDialog) {
            showLogoutDialog = false
        } else if (selectedIndex != 0) {
            selectedIndex = 0
            lastBackPressTime = 0L
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
            if (enabledModules.isNotEmpty()) {
                NavigationBar(containerColor = fondoVerde, contentColor = Color.White) {
                    enabledModules.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = { Text(text = item.second, fontSize = 20.sp) },
                            label = {
                                Text(
                                    text = item.first,
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
        }
    ) { innerPadding ->
        if (enabledModules.isNotEmpty()) {
            val moduleIndex = if (selectedIndex >= enabledModules.size) 0 else selectedIndex
            enabledModules[moduleIndex].third(Modifier.padding(innerPadding))
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No hay módulos seleccionados", color = Color.White)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = stringResource(R.string.dialog_logout_title), fontWeight = FontWeight.Bold) },
            text = { Text(text = stringResource(R.string.dialog_logout_message)) },
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
                    Text(stringResource(R.string.btn_cancel), color = Color.Gray, fontWeight = FontWeight.Medium)
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
