package com.arstudios.fliigo.debt.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
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
import com.arstudios.fliigo.debt.data.DebtDto
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DebtItemCard(
    debt: DebtDto,
    textoOscuro: Color,
    verdeExito: Color,
    rojoGasto: Color,
    onMarkAsPaid: () -> Unit,
    onDelete: () -> Unit,
    onSendReminder: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxRevealPx = with(density) { 180.dp.toPx() } 
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        // --- ACCIONES DETRÁS ---
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFEF5350))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón Recordatorio (Azul)
            if (!debt.phone.isNullOrBlank()) {
                IconButton(
                    onClick = {
                        coroutineScope.launch { offsetX.animateTo(0f) }
                        onSendReminder()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                }
            }

            IconButton(
                onClick = {
                    coroutineScope.launch { offsetX.animateTo(0f) }
                    onMarkAsPaid()
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = {
                    coroutineScope.launch { offsetX.animateTo(0f) }
                    onDelete()
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
                                if (offsetX.value < -maxRevealPx / 2) offsetX.animateTo(-maxRevealPx)
                                else offsetX.animateTo(0f)
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
                        text = debt.clientName ?: "Sin nombre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textoOscuro
                    )
                    Text(
                        text = debt.description ?: (if (debt.type == "receivable") "Por cobrar" else "Por pagar"),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$ ${String.format(Locale.US, "%.1f", debt.amount ?: 0.0)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (debt.type == "receivable") verdeExito else rojoGasto
                    )
                    if (debt.createdAt != null) {
                        Text(
                            text = if (debt.createdAt.length >= 10) debt.createdAt.take(10) else debt.createdAt,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
