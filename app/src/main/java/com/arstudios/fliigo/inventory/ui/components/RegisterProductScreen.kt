package com.arstudios.fliigo.inventory.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arstudios.fliigo.core.ui.components.BarcodeScannerView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fn RegisterProductScreen(
onSaveProduct: (
id: String,
name: String,
salePrice: String,
cost: String,
stock: String,
supplier: String,
category: String
) -> Unit,
onBack: () -> Unit
) {
    val context = LocalContext.current

    // Estados de los campos del formulario
    var productId by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }

    // Estados de control para la cámara y categorías
    var showCameraScanner by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    // Lista de ejemplo de categorías (puedes adaptarla a tu base de datos)
    val categories = listOf("General", "Bebidas", "Alimentos", "Limpieza", "Tecnología")

    // Selector de imagen desde la Galería para leer código de barras
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { code ->
                                productId = code // Rellena el ID automáticamente
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Manejar error de lectura de galería
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showCameraScanner) {
        // Pantalla de la cámara a pantalla completa para escanear
        Box(modifier = Modifier.fillMaxSize()) {
            BarcodeScannerView(
                onBarcodeDetected = { code ->
                    productId = code
                    showCameraScanner = false
                }
            )
            // Botón flotante para cerrar la cámara si se arrepiente
            Button(
                onClick = { showCameraScanner = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                Text("Cancelar Escáner")
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Registrar Producto") },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text("Atrás")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo ID del producto + Botones de Cámara y Galería
                OutlinedTextField(
                    value = productId,
                    onValueChange = { productId = it },
                    label = { Text("ID / Código de Barras") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { showCameraScanner = true }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Escanear con Cámara")
                            }
                            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(Icons.Default.Image, contentDescription = "Escanear desde Galería")
                            }
                        }
                    }
                )

                // Nombre del producto
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nombre del Producto") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Precio de Venta
                OutlinedTextField(
                    value = salePrice,
                    onValueChange = { salePrice = it },
                    label = { Text("Precio de Venta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Costo
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Costo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Stock inicial
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock Inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Proveedor
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Proveedor") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector de Categoría (ExposedDropdownMenu)
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        onSaveProduct(
                            productId,
                            productName,
                            salePrice,
                            cost,
                            stock,
                            supplier,
                            selectedCategory
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Producto")
                }
            }
        }
    }
}