package com.arstudios.fliigo.debt.ui.components

import androidx.compose.foundation.layout.*
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

@Composable
fun AddDebtDialog(
    type: String, // "receivable" o "payable"
    onDismiss: () -> Unit,
    onConfirm: (client: String, amount: Double, description: String) -> Unit
) {
    var client by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val textoOscuro = colorResource(R.color.texto_oscuro)
    val botonesOscuros = colorResource(R.color.botones_oscuros)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (type == "receivable") "Nueva Cuenta por Cobrar" else "Nueva Cuenta por Pagar",
                fontWeight = FontWeight.Bold,
                color = textoOscuro
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Nombre del Cliente/Proveedor") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textoOscuro, unfocusedTextColor = textoOscuro)
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textoOscuro, unfocusedTextColor = textoOscuro)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción / Concepto") },
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
                    if (client.isNotBlank() && amount > 0) {
                        onConfirm(client, amount, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = botonesOscuros),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
