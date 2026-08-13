package com.arstudios.fliigo.balance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.data.SaleItem
import com.arstudios.fliigo.balance.ui.components.BalanceHeader
import com.arstudios.fliigo.balance.ui.components.BalanceSummaryCard
import com.arstudios.fliigo.balance.ui.components.DeleteSaleConfirmationDialog
import com.arstudios.fliigo.balance.ui.components.InvoiceDialog
import com.arstudios.fliigo.balance.ui.components.RegisterSaleDialog
import com.arstudios.fliigo.balance.ui.components.SaleItemCard
import com.arstudios.fliigo.balance.viewmodel.BalanceUiState
import com.arstudios.fliigo.balance.viewmodel.BalanceViewModel
import com.arstudios.fliigo.core.ui.components.BarcodeScannerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(
    modifier: Modifier = Modifier,
    viewModel: BalanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val productErrorMessage = viewModel.productErrorMessage
    val isLoadingProducts = viewModel.isLoadingProducts

    var showRegisterDialog by remember { mutableStateOf(false) }
    var saleToViewInvoice by remember { mutableStateOf<SaleItem?>(null) }
    var saleToDelete by remember { mutableStateOf<SaleItem?>(null) }
    val saleDeletedSuccessMessage = stringResource(R.string.sale_deleted_success)

    val fondoVerde = colorResource(R.color.fondo_verde)
    val amarilloHeader = colorResource(R.color.amarillo_header)
    val botonesOscuros = colorResource(R.color.botones_oscuros)
    val textoOscuro = colorResource(R.color.texto_oscuro)
    val verdeExito = colorResource(R.color.verde_exito)
    val rojoGasto = colorResource(R.color.rojo_gasto)
    val grisFondo = colorResource(R.color.gris_fondo)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondoVerde)
        ) {
            val storeTitle = when (state) {
                is BalanceUiState.Success -> state.storeName
                else -> stringResource(R.string.store_name)
            }

            BalanceHeader(
                storeTitle = storeTitle,
                amarilloHeader = amarilloHeader
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = grisFondo)
            ) {
                PullToRefreshBox(
                    isRefreshing = viewModel.isRefreshing,
                    onRefresh = { viewModel.loadStoreBalance(refreshing = true) },
                    modifier = Modifier.fillMaxSize()
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
                                                Text(
                                                    stringResource(R.string.btn_retry),
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                BalanceSummaryCard(
                                    balance = state.balance,
                                    totalIncome = state.totalIncome,
                                    totalExpenses = state.totalExpenses,
                                    textoOscuro = textoOscuro,
                                    verdeExito = verdeExito,
                                    rojoGasto = rojoGasto
                                )

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
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(bottom = 80.dp)
                                    ) {
                                        items(state.salesList) { sale ->
                                            SaleItemCard(
                                                sale = sale,
                                                textoOscuro = textoOscuro,
                                                verdeExito = verdeExito,
                                                onViewInvoice = { saleToViewInvoice = sale },
                                                onDeleteSale = { saleToDelete = sale }
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

        FloatingActionButton(
            onClick = {
                viewModel.loadStoreProducts()
                showRegisterDialog = true
            },
            containerColor = botonesOscuros,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.cd_register_sale_fab)
            )
        }

        if (showRegisterDialog) {
            RegisterSaleDialog(
                onDismiss = {
                    showRegisterDialog = false
                    viewModel.loadStoreBalance()
                },
                onOpenBarcodeScanner = { rowIndex: Int ->
                    viewModel.updateActiveRowIndex(rowIndex)
                    viewModel.setScannerModalVisibility(true)
                },
                viewModel = viewModel
            )
        }

        if (viewModel.showScannerModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                BarcodeScannerView(
                    onBarcodeDetected = { scannedCode ->
                        val activeIdx = viewModel.activeRowIndex
                        if (activeIdx != null && scannedCode.isNotBlank()) {
                            viewModel.deliverScannedCode(activeIdx, scannedCode)
                        }
                        viewModel.setScannerModalVisibility(false)
                    }
                )
            }
        }

        // --- DIÁLOGO DE FACTURA ---
        if (saleToViewInvoice != null && state is BalanceUiState.Success) {
            InvoiceDialog(
                sale = saleToViewInvoice!!,
                storeName = state.storeName,
                onDismiss = { saleToViewInvoice = null }
            )
        }

        // --- DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN ---
        if (saleToDelete != null) {
            DeleteSaleConfirmationDialog(
                onConfirm = {
                    viewModel.deleteSale(
                        saleId = saleToDelete!!.id,
                        onSuccess = {
                            Toast.makeText(context, saleDeletedSuccessMessage, Toast.LENGTH_SHORT).show()
                            saleToDelete = null
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            saleToDelete = null
                        }
                    )
                },
                onDismiss = { saleToDelete = null }
            )
        }
    }
}
