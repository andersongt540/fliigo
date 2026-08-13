package com.arstudios.fliigo.debt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.debt.data.ClientSuggestionDto

@Composable
fun AddDebtDialog(
    type: String,
    suggestions: List<ClientSuggestionDto>,
    onDismiss: () -> Unit,
    onConfirm: (client: String, amount: Double, description: String, phone: String?) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val textoOscuro = colorResource(R.color.texto_oscuro)
    val botonesOscuros = colorResource(R.color.botones_oscuros)

    val filteredSuggestions = remember(clientName) {
        if (clientName.length >= 2) {
            suggestions.filter { it.clientName.contains(clientName, ignoreCase = true) }
        } else emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (type == "receivable") stringResource(R.string.dialog_add_debt_receivable) else stringResource(R.string.dialog_add_debt_payable),
                fontWeight = FontWeight.Bold,
                color = textoOscuro
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Nombre del Cliente con Autocompletado
                Column {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text(stringResource(R.string.label_debt_client)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textoOscuro, unfocusedTextColor = textoOscuro)
                    )
                    
                    if (filteredSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            LazyColumn {
                                items(filteredSuggestions) { suggestion ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                clientName = suggestion.clientName
                                                phone = suggestion.phone ?: ""
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(suggestion.clientName, fontWeight = FontWeight.Medium, color = textoOscuro)
                                        if (!suggestion.phone.isNullOrBlank()) {
                                            Text("Tel: ${suggestion.phone}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Campo de Teléfono (Se auto-rellena o se escribe manual)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.phone_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textoOscuro, unfocusedTextColor = textoOscuro)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(stringResource(R.string.label_debt_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textoOscuro, unfocusedTextColor = textoOscuro)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.label_debt_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textoOscuro, unfocusedTextColor = textoOscuro)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (clientName.isNotBlank() && amount > 0) {
                        onConfirm(clientName, amount, description, phone.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = botonesOscuros),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
