package com.arstudios.fliigo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BarcodeScannerModal(
    onDismiss: () -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Cuadro del escáner
                Card(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    BarcodeScannerView(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeDetected = onBarcodeDetected
                    )
                }

                // Botón de cerrar
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, RoundedCornerShape(32.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
