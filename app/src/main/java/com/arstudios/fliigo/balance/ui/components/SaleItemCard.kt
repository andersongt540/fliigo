package com.arstudios.fliigo.balance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.data.SaleItem
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SaleItemCard(
    sale: SaleItem,
    textoOscuro: Color,
    verdeExito: Color,
    onViewInvoice: () -> Unit,
    onDeleteSale: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxRevealPx = with(density) { 140.dp.toPx() }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // --- ACCIONES DETRÁS (SE REVELAN AL SWIPE) ---
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFEF5350)) // Fondo rojo para indicar eliminación
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Ver Factura
            IconButton(
                onClick = {
                    coroutineScope.launch { offsetX.animateTo(0f) }
                    onViewInvoice()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = stringResource(R.string.cd_invoice),
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
            }
            // Botón Eliminar
            IconButton(
                onClick = {
                    coroutineScope.launch { offsetX.animateTo(0f) }
                    onDeleteSale()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete_sale),
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- TARJETA PRINCIPAL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceIn(-maxRevealPx, 0f)
                                offsetX.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value < -maxRevealPx / 2) {
                                    offsetX.animateTo(-maxRevealPx)
                                } else {
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    )
                },
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sale.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textoOscuro
                    )
                    Text(
                        text = sale.clientName,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "$ ${String.format(Locale.US, "%.1f", sale.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = verdeExito
                )
            }
        }
    }
}
