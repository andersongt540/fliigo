package com.arstudios.fliigo.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.ui.screens.BalanceScreen
import com.arstudios.fliigo.debt.ui.DebtScreen
import com.arstudios.fliigo.enDesarrollo.UnderDevelopmentScreen
import com.arstudios.fliigo.inventory.ui.screens.InventoryScreen

@Composable
fun MainScreen() {
    var selectedIndex by remember { mutableIntStateOf(0) }

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
}