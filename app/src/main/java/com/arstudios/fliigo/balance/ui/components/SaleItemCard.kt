package com.arstudios.fliigo.balance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.balance.data.GroupedSale
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SaleItemCard(
    sale: GroupedSale,
    textoOscuro: Color,
    verdeExito: Color,
    onViewInvoice: () -> Unit,
    onDeleteSale: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
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
        // Solo permitimos swipe si NO está expandido
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFEF5350))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch { offsetX.animateTo(0f) }
                    onViewInvoice()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = {
                    coroutineScope.launch { offsetX.animateTo(0f) }
                    onDeleteSale()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
            }
        }

        // --- TARJETA PRINCIPAL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .pointerInput(isExpanded) {
                    // Si está expandido, no permitimos el gesto de deslizar
                    if (!isExpanded) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                coroutineScope.launch {
                                    val newOffset = (offsetX.value + dragAmount).coerceIn(-maxRevealPx, 0f)
                                    offsetX.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (offsetX.value < -maxRevealPx / 2) offsetX.animateTo(-maxRevealPx)
                                    else offsetX.animateTo(0f)
                                }
                            }
                        )
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sale.clientName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = textoOscuro
                        )
                        Text(
                            text = if (sale.items.size == 1) sale.items.first().productName else "${sale.items.size} productos",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$ ${String.format(Locale.US, "%.2f", sale.totalAmount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = verdeExito
                        )
                        
                        if (sale.items.size > 1) {
                            IconButton(onClick = { isExpanded = !isExpanded }) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = textoOscuro
                                )
                            }
                        } else {
                            // Espacio para mantener alineación si solo hay 1 producto
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F8F8))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sale.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.productName} (x${item.quantity})",
                                    fontSize = 13.sp,
                                    color = textoOscuro,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$ ${String.format(Locale.US, "%.2f", item.amount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textoOscuro
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
