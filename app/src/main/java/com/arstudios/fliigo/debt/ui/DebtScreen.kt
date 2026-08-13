package com.arstudios.fliigo.debt.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.debt.data.DebtDto
import com.arstudios.fliigo.debt.ui.components.AddDebtDialog
import com.arstudios.fliigo.debt.ui.components.DebtItemCard
import com.arstudios.fliigo.debt.ui.components.DeleteDebtConfirmationDialog
import com.arstudios.fliigo.debt.viewmodel.DebtUiState
import com.arstudios.fliigo.debt.viewmodel.DebtViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    modifier: Modifier = Modifier,
    viewModel: DebtViewModel = viewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState

    val fondoVerde = colorResource(R.color.fondo_verde)
    val amarilloHeader = colorResource(R.color.amarillo_header)
    val botonesOscuros = colorResource(R.color.botones_oscuros)
    val textoOscuro = colorResource(R.color.texto_oscuro)
    val verdeExito = colorResource(R.color.verde_exito)
    val rojoGasto = colorResource(R.color.rojo_gasto)
    val grisFondo = colorResource(R.color.gris_fondo)

    var showAddDebtDialog by remember { mutableStateOf<String?>(null) } // "receivable" o "payable"
    var debtToDelete by remember { mutableStateOf<DebtDto?>(null) }

    val debtRegisteredMessage = stringResource(R.string.debt_registered_success)
    val debtMarkedPaidMessage = stringResource(R.string.debt_marked_paid_success)
    val debtDeletedMessage = stringResource(R.string.debt_deleted_success)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(fondoVerde)
    ) {
        // --- ENCABEZADO ---
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

            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- CONTENIDO ---
        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = grisFondo)
        ) {
            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.loadDebtsData(refreshing = true) }
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
                        val filteredDebts = state.debtList.filter {
                            (it.clientName?.contains(state.searchQuery, ignoreCase = true) ?: false) ||
                            (it.description?.contains(state.searchQuery, ignoreCase = true) ?: false)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
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
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🟢 " + stringResource(R.string.label_receivable), fontSize = 12.sp, color = Color.Gray)
                                        Text(
                                            text = "$ ${String.format(Locale.US, "%.1f", state.totalReceivable)}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textoOscuro
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
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🔴 " + stringResource(R.string.label_payable), fontSize = 12.sp, color = Color.Gray)
                                        Text(
                                            text = "$ ${String.format(Locale.US, "%.1f", state.totalPayable)}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textoOscuro
                                        )
                                    }
                                }
                            }

                            // Barra de búsqueda
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text(stringResource(R.string.search_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textoOscuro,
                                    unfocusedTextColor = textoOscuro,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )

                            // Lista
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(filteredDebts, key = { it.id ?: it.hashCode() }) { debt ->
                                    DebtItemCard(
                                        debt = debt,
                                        textoOscuro = textoOscuro,
                                        verdeExito = verdeExito,
                                        rojoGasto = rojoGasto,
                                        onMarkAsPaid = {
                                            debt.id?.let {
                                                viewModel.markAsPaid(it, onSuccess = {
                                                    Toast.makeText(context, debtMarkedPaidMessage, Toast.LENGTH_SHORT).show()
                                                })
                                            }
                                        },
                                        onDelete = {
                                            debtToDelete = debt
                                        },
                                        onSendReminder = {
                                            sendWhatsAppReminder(context, debt)
                                        }
                                    )
                                }
                            }

                            // Botones inferiores
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { 
                                        viewModel.loadClientSuggestions()
                                        showAddDebtDialog = "receivable" 
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = verdeExito)
                                ) {
                                    Text("COBRAR", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { 
                                        viewModel.loadClientSuggestions()
                                        showAddDebtDialog = "payable" 
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = rojoGasto)
                                ) {
                                    Text("PAGAR", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDebtDialog != null && state is DebtUiState.Success) {
        AddDebtDialog(
            type = showAddDebtDialog!!,
            suggestions = state.clientSuggestions,
            onDismiss = { showAddDebtDialog = null },
            onConfirm = { client, amount, desc, phone ->
                viewModel.registerDebt(
                    client = client,
                    amount = amount,
                    description = desc,
                    phone = phone,
                    type = showAddDebtDialog!!,
                    onSuccess = {
                        showAddDebtDialog = null
                        Toast.makeText(context, debtRegisteredMessage, Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (debtToDelete != null) {
        DeleteDebtConfirmationDialog(
            onConfirm = {
                debtToDelete?.id?.let {
                    viewModel.deleteDebt(it, onSuccess = {
                        Toast.makeText(context, debtDeletedMessage, Toast.LENGTH_SHORT).show()
                    })
                }
                debtToDelete = null
            },
            onDismiss = { debtToDelete = null }
        )
    }
}

private fun sendWhatsAppReminder(context: Context, debt: DebtDto) {
    val phone = debt.phone ?: return
    val message = "Hola ${debt.clientName}, te escribo de Fliigo para recordarte tu saldo pendiente de $ ${String.format(Locale.US, "%.2f", debt.amount)}. Saludos!"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=" + Uri.encode(message)))
    context.startActivity(intent)
}
