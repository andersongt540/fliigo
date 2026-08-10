package com.arstudios.fliigo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.viewmodel.BalanceUiState
import com.arstudios.fliigo.viewmodel.BalanceViewModel
import java.util.Locale

@Composable
fun BalanceScreen(
    modifier: Modifier = Modifier,
    viewModel: BalanceViewModel = viewModel()
) {
    val state = viewModel.uiState
    val productErrorMessage = viewModel.productErrorMessage
    val isLoadingProducts = viewModel.isLoadingProducts

    val fondoVerde = colorResource(R.color.fondo_verde)
    val amarilloHeader = colorResource(R.color.amarillo_header)
    val botonesOscuros = colorResource(R.color.botones_oscuros)
    val textoOscuro = colorResource(R.color.texto_oscuro)
    val verdeExito = colorResource(R.color.verde_exito)
    val rojoGasto = colorResource(R.color.rojo_gasto)
    val grisFondo = colorResource(R.color.gris_fondo)

    // Opcional: Puedes disparar la carga de productos al entrar a la pantalla si lo requieres
    // LaunchedEffect(Unit) {
    //     viewModel.loadStoreProducts()
    // }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fondoVerde)
    ) {
        // --- ENCABEZADO SUPERIOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 20.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val storeTitle = when(state) {
                    is BalanceUiState.Success -> state.storeName
                    else -> stringResource(R.string.store_name)
                }
                Text(
                    text = storeTitle.uppercase(),
                    color = amarilloHeader,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.owner_role),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("❓", fontSize = 20.sp)
                Text("🔔", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- CONTENEDOR PRINCIPAL INFERIOR ---
        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = grisFondo)
        ) {
            when (state) {
                is BalanceUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = botonesOscuros)
                    }
                }
                is BalanceUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadStoreBalance() }) {
                                Text(stringResource(R.string.btn_retry))
                            }
                        }
                    }
                }
                is BalanceUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- ALERTA DE ERROR DE PRODUCTOS (Si aplica) ---
                        if (productErrorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = productErrorMessage,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.loadStoreProducts() }) {
                                        Text("Reintentar", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Resumen Financiero Diario (Utilidad, Ventas y Gastos del Día)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.label_today_utility), color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$ ${String.format(Locale.US, "%.1f", state.balance)}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textoOscuro
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(1.dp)
                                        .background(Color.LightGray)
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🟢 ", fontSize = 10.sp)
                                        Column {
                                            Text(stringResource(R.string.total_sales), fontSize = 11.sp, color = Color.Gray)
                                            Text("$ ${String.format(Locale.US, "%.1f", state.totalIncome)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = verdeExito)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🔴 ", fontSize = 10.sp)
                                        Column {
                                            Text(stringResource(R.string.total_expenses), fontSize = 11.sp, color = Color.Gray)
                                            Text("$ ${String.format(Locale.US, "%.1f", state.totalExpenses)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = rojoGasto)
                                        }
                                    }
                                }
                            }
                        }

                        // Título de la sección de ventas recientes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.title_recent_sales),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textoOscuro
                            )

                            if (isLoadingProducts) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = botonesOscuros
                                )
                            }
                        }

                        // Lista Real de Ventas obtenidas de la Base de Datos
                        if (state.salesList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_sales_message),
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.salesList) { sale ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sale.productName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = textoOscuro,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = sale.clientName,
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "$ ${String.format(Locale.US, "%.1f", sale.amount)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = verdeExito
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}