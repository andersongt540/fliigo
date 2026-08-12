package com.arstudios.fliigo.SetupStore.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.core.theme.AmarilloHeader
import com.arstudios.fliigo.core.theme.FondoVerde
import com.arstudios.fliigo.core.theme.TextoOscuro

data class ModuleItem(
    val id: String,
    val nameRes: Int,
    val descRes: Int,
    val icon: String
)

@Composable
fun ModuleSelectionScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val modules = listOf(
        ModuleItem("balance", R.string.module_balance_name, R.string.module_balance_desc, "🧮"),
        ModuleItem("debt", R.string.module_debt_name, R.string.module_debt_desc, "💼"),
        ModuleItem("stats", R.string.module_stats_name, R.string.module_stats_desc, "📊"),
        ModuleItem("inventory", R.string.module_inventory_name, R.string.module_inventory_desc, "📋")
    )

    var selectedModules by remember { mutableStateOf(modules.map { it.id }.toSet()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoVerde)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmarilloHeader, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.module_selection_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Text(
                    text = stringResource(R.string.module_selection_subtitle),
                    fontSize = 14.sp,
                    color = TextoOscuro.copy(alpha = 0.7f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(modules) { module ->
                val isSelected = selectedModules.contains(module.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedModules = if (isSelected) {
                                selectedModules - module.id
                            } else {
                                selectedModules + module.id
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = module.icon, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(module.nameRes),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextoOscuro
                            )
                            Text(
                                text = stringResource(module.descRes),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                saveModulePreferences(context, selectedModules)
                onFinish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AmarilloHeader)
        ) {
            Text(
                text = stringResource(R.string.btn_finish_setup),
                color = TextoOscuro,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

private fun saveModulePreferences(context: Context, selectedIds: Set<String>) {
    val prefs = context.getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
    prefs.edit().apply {
        putBoolean("module_balance", selectedIds.contains("balance"))
        putBoolean("module_debt", selectedIds.contains("debt"))
        putBoolean("module_stats", selectedIds.contains("stats"))
        putBoolean("module_inventory", selectedIds.contains("inventory"))
        apply()
    }
}
