package com.arstudios.fliigo.balance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.viewmodel.BalanceViewModel
import java.util.Locale

data class SaleItemRow(
    var productId: String = "",
    var quantity: String = "1"
)

@Composable
fun RegisterSaleDialog(
    onDismiss: () -> Unit,
    onOpenBarcodeScanner: (Int) -> Unit,
    viewModel: BalanceViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    val productRows = remember { mutableStateListOf(SaleItemRow()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val botonesOscuros = colorResource(R.color.botones_oscuros)
    val verdeExito = colorResource(R.color.verde_exito)
    val textoOscuro = colorResource(R.color.texto_oscuro)

    val calculatedSubtotals = productRows.map { row ->
        val pIdInt = row.productId.toIntOrNull()
        val qty = row.quantity.toIntOrNull() ?: 0
        val product = viewModel.productList.find { it.id == pIdInt }
        val unitPrice = product?.price ?: 0.0
        val totalRow = unitPrice * qty
        Triple(product, unitPrice, totalRow)
    }

    val grandTotal = calculatedSubtotals.sumOf { it.third }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.register_sale_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textoOscuro
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(stringResource(R.string.client_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text(stringResource(R.string.address_label)) },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text(stringResource(R.string.phone_label)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.products_label),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textoOscuro
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(productRows) { index, row ->
                        val (product, unitPrice, totalRow) = calculatedSubtotals[index]

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = row.productId,
                                        onValueChange = { newValue ->
                                            productRows[index] = row.copy(productId = newValue)
                                        },
                                        label = { Text(stringResource(R.string.product_id_format, index + 1)) },
                                        modifier = Modifier.weight(2f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    IconButton(
                                        onClick = { onOpenBarcodeScanner(index) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(botonesOscuros.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = stringResource(R.string.cd_scan_barcode),
                                            tint = botonesOscuros
                                        )
                                    }

                                    OutlinedTextField(
                                        value = row.quantity,
                                        onValueChange = { newValue ->
                                            productRows[index] = row.copy(quantity = newValue)
                                        },
                                        label = { Text(stringResource(R.string.quantity_short_label)) },
                                        modifier = Modifier.weight(1.1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    if (productRows.size > 1) {
                                        IconButton(
                                            onClick = {
                                                productRows.removeAt(index)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.cd_delete_item),
                                                tint = Color.Red.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = product?.name ?: if (row.productId.isBlank()) stringResource(R.string.hint_enter_code) else stringResource(R.string.error_product_not_found),
                                        fontSize = 12.sp,
                                        color = if (product != null) textoOscuro else Color.Red.copy(alpha = 0.8f),
                                        fontWeight = if (product != null) FontWeight.Medium else FontWeight.Bold
                                    )

                                    if (product != null) {
                                        Text(
                                            text = stringResource(R.string.price_subtotal_format, String.format(Locale.US, "%.2f", unitPrice), String.format(Locale.US, "%.2f", totalRow)),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = botonesOscuros
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = botonesOscuros.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.total_sale_label),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textoOscuro
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", grandTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = botonesOscuros
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            productRows.add(SaleItemRow())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = botonesOscuros)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_add_another_product), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = botonesOscuros)
                        ) {
                            Text(stringResource(R.string.btn_cancel), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                viewModel.registerSaleWithValidation(
                                    clientName = nombre,
                                    address = direccion,
                                    phone = telefono,
                                    productRows = productRows,
                                    onSuccess = {
                                        isLoading = false
                                        onDismiss()
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = error
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            enabled = !isLoading && nombre.isNotBlank() && productRows.all { it.productId.isNotBlank() },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = verdeExito)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(stringResource(R.string.btn_register_sale), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}