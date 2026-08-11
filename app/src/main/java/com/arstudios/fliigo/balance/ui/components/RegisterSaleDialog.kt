package com.arstudios.fliigo.balance.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.viewmodel.BalanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSaleDialog(
    onDismiss: () -> Unit,
    viewModel: BalanceViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var productIdText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }

    var isLoading by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Registrar Venta", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = productIdText,
                onValueChange = { productIdText = it },
                label = { Text("ID de Producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    val pId = productIdText.toIntOrNull() ?: 0
                    val qty = quantityText.toIntOrNull() ?: 1

                    isLoading = true
                    viewModel.registerSale(
                        clientName = nombre,
                        address = direccion,
                        phone = telefono,
                        productId = pId,
                        quantity = qty,
                        onSuccess = {
                            isLoading = false
                            onDismiss()
                        },
                        onError = { _ ->
                            isLoading = false
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = !isLoading && nombre.isNotBlank() && productIdText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.verde_exito))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Registrar Venta")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}