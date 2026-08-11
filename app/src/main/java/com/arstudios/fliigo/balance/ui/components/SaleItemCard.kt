package com.arstudios.fliigo.balance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.balance.data.SaleItem // Importa la clase correcta
import java.util.Locale

@Composable
fun SaleItemCard(
    sale: SaleItem, // Cambiado de SaleItemUiState a SaleItem
    textoOscuro: Color,
    verdeExito: Color
) {
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
            // Asegúrate de que los nombres de las propiedades (productName, clientName, amount)
            // coincidan exactamente con cómo están definidos en tu clase SaleItem
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