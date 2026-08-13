package com.arstudios.fliigo.balance.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.data.GroupedSale
import java.util.Locale

@Composable
fun InvoiceDialog(
    sale: GroupedSale,
    storeName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedTotal = "$ ${String.format(Locale.US, "%.2f", sale.totalAmount)}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.title_invoice),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(R.string.store_name) + ": $storeName", fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.invoice_date, sale.date), fontSize = 12.sp)
                Text(text = stringResource(R.string.invoice_client, sale.clientName), fontWeight = FontWeight.Medium)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                // Lista de productos
                sale.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${item.productName} (x${item.quantity})", fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(text = "$ ${String.format(Locale.US, "%.2f", item.amount)}", fontSize = 13.sp)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "TOTAL", fontWeight = FontWeight.Bold)
                    Text(
                        text = formattedTotal,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (sale.address.isNotBlank()) {
                    Text(text = stringResource(R.string.invoice_address, sale.address), fontSize = 12.sp)
                }
                if (sale.phone.isNotBlank()) {
                    Text(text = stringResource(R.string.invoice_phone, sale.phone), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        shareViaWhatsApp(context, sale, storeName, formattedTotal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_share_whatsapp))
                }
                OutlinedButton(
                    onClick = {
                        shareViaEmail(context, sale, storeName, formattedTotal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_share_email))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

private fun shareViaWhatsApp(context: Context, sale: GroupedSale, storeName: String, total: String) {
    val itemsDetail = sale.items.joinToString("\n") { "- ${it.productName} (x${it.quantity}): $ ${String.format(Locale.US, "%.2f", it.amount)}" }
    val message = "Factura de $storeName\n\n" +
                  "Cliente: ${sale.clientName}\n" +
                  "Fecha: ${sale.date}\n" +
                  "------------------\n" +
                  "$itemsDetail\n" +
                  "------------------\n" +
                  "TOTAL: $total"
    
    val uri = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

private fun shareViaEmail(context: Context, sale: GroupedSale, storeName: String, total: String) {
    val itemsDetail = sale.items.joinToString("\n") { "- ${it.productName} (x${it.quantity}): $ ${String.format(Locale.US, "%.2f", it.amount)}" }
    val message = "Factura de $storeName\n\n" +
                  "Cliente: ${sale.clientName}\n" +
                  "Fecha: ${sale.date}\n" +
                  "------------------\n" +
                  "$itemsDetail\n" +
                  "------------------\n" +
                  "TOTAL: $total"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.title_invoice) + " - " + storeName)
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Enviar factura"))
}
