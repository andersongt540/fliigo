package com.arstudios.fliigo.debt.ui

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
import com.arstudios.fliigo.debt.viewmodel.DebtUiState
import com.arstudios.fliigo.debt.viewmodel.DebtViewModel
import java.util.Locale

@Composable
fun DebtScreen(
    modifier: Modifier = Modifier,
    viewModel: DebtViewModel = viewModel()
) {
    val state = viewModel.uiState

    val fondoVerde = colorResource(R.color.fondo_verde)
    val amarilloHeader = colorResource(R.color.amarillo_header)
    val botonesOscuros = colorResource(R.color.botones_oscuros)
    val textoOscuro = colorResource(R.color.texto_oscuro)
    val verdeExito = colorResource(R.color.verde_exito)
    val rojoGasto = colorResource(R.color.rojo_gasto)
    val grisFondo = colorResource(R.color.gris_fondo)

    // Contenedor principal que recibe el padding del Scaffold central
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
                    is DebtUiState.Success -> state.storeName
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

        Spacer(modifier = Modifier.height(4.dp))

        // --- CONTENEDOR PRINCIPAL INFERIOR ---
        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = grisFondo)
        ) {
            when (state) {
                is DebtUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = botonesOscuros)
                    }
                }
                is DebtUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadDebtsData() }) {
                                Text(stringResource(R.string.btn_retry))
                            }
                        }
                    }
                }
                is DebtUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tarjetas superiores: Por Cobrar / Por Pagar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Por Cobrar
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🟢 ", fontSize = 12.sp)
                                        Text(stringResource(R.string.label_receivable), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textoOscuro)
                                    }
                                    Text(
                                        text = "$ ${String.format(Locale.US, "%.1f", state.totalReceivable)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textoOscuro
                                    )
                                    Text(
                                        text = "${state.receivableClientsCount} ${stringResource(R.string.label_clients)}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Por Pagar
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🔴 ", fontSize = 12.sp)
                                        Text(stringResource(R.string.label_payable), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textoOscuro)
                                    }
                                    Text(
                                        text = "$ ${String.format(Locale.US, "%.1f", state.totalPayable)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textoOscuro
                                    )
                                    Text(
                                        text = "${state.payableClientsCount} ${stringResource(R.string.label_clients)}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Barra de Búsqueda de Registros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text(stringResource(R.string.search_hint), fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                            ) {
                                Text("📥", fontSize = 18.sp)
                            }
                        }

                        // Lista de Registros de Deuda
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.debtList) { debt ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(verdeExito, shape = CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("C", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Column {
                                                Text(text = debt.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textoOscuro)
                                                Text(text = debt.timeAgo, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "$ ${String.format(Locale.US, "%.1f", debt.amount)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = verdeExito
                                            )
                                            Text(
                                                text = debt.paymentMethod,
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Botones de Acción inferior (VENTA y GASTO)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = verdeExito)
                            ) {
                                Text(stringResource(R.string.btn_sale), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = rojoGasto)
                            ) {
                                Text(stringResource(R.string.btn_expense), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}