package com.arstudios.fliigo.balance.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.data.SaleItem
import java.util.Locale

@Composable
fun InvoiceDialog(
    sale: SaleItem,
    storeName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedAmount = "$ ${String.format(Locale.US, "%.2f", sale.amount)}"

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = stringResource(R.string.store_name) + ": $storeName", fontWeight = FontWeight.Medium)
                HorizontalDivider()
                Text(text = stringResource(R.string.invoice_date, sale.date))
                Text(text = stringResource(R.string.invoice_client, sale.clientName))
                Text(text = stringResource(R.string.invoice_product, sale.productName))
                Text(
                    text = stringResource(R.string.invoice_total, formattedAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (sale.address.isNotBlank()) {
                    Text(text = stringResource(R.string.invoice_address, sale.address))
                }
                if (sale.phone.isNotBlank()) {
                    Text(text = stringResource(R.string.invoice_phone, sale.phone))
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { shareViaWhatsApp(context, sale, storeName, formattedAmount) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.btn_share_whatsapp))
                }
                OutlinedButton(
                    onClick = { shareViaEmail(context, sale, storeName, formattedAmount) },
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

private fun shareViaWhatsApp(context: Context, sale: SaleItem, storeName: String, amount: String) {
    val message = context.getString(
        R.string.invoice_share_text,
        storeName,
        sale.clientName,
        sale.productName,
        1, // La cantidad ya está implícita en la cadena de productos
        amount,
        sale.date
    )
    val uri = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

private fun shareViaEmail(context: Context, sale: SaleItem, storeName: String, amount: String) {
    val message = context.getString(
        R.string.invoice_share_text,
        storeName,
        sale.clientName,
        sale.productName,
        1,
        amount,
        sale.date
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.title_invoice) + " - " + storeName)
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Enviar factura"))
}
