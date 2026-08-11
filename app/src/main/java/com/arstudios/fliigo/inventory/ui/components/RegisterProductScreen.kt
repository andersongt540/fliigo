package com.arstudios.fliigo.inventory.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.core.ui.components.BarcodeScannerView
import com.arstudios.fliigo.inventory.data.CategoryDto
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductScreen(
    categories: List<CategoryDto>,
    onSave: (
        name: String,
        price: Double,
        costPrice: Double?,
        provider: String?,
        stock: Int,
        category: String?
    ) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Estados de los campos del formulario
    var productId by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var salePriceStr by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Estados de control para la cámara y categorías
    var showCameraScanner by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                                productId = code
                            }
                        }
                    }
                    .addOnFailureListener {
                        errorMessage = "No se pudo leer el código de la imagen"
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showCameraScanner) {
        Box(modifier = Modifier.fillMaxSize()) {
            BarcodeScannerView(
                onBarcodeDetected = { code ->
                    productId = code
                    showCameraScanner = false
                }
            )
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
                // Campo ID / Código de Barras
                OutlinedTextField(
                    value = productId,
                    onValueChange = { productId = it },
                    label = { Text("ID / Código de Barras (Opcional)") },
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Precio de Venta
                    OutlinedTextField(
                        value = salePriceStr,
                        onValueChange = { salePriceStr = it },
                        label = { Text("Precio de Venta ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // Costo
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Costo ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stock inicial
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Inicial") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // Proveedor
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Proveedor") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Selector de Categoría (ExposedDropdownMenu)
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory ?: "Seleccionar Categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.name
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        val price = salePriceStr.toDoubleOrNull()
                        val costPrice = costStr.toDoubleOrNull()
                        val stock = stockStr.toIntOrNull()

                        when {
                            productName.isBlank() -> errorMessage = "Ingresa un nombre de producto válido"
                            price == null || price <= 0.0 -> errorMessage = "Ingresa un precio de venta válido"
                            stock == null || stock < 0 -> errorMessage = "Ingresa un stock inicial válido"
                            else -> {
                                onSave(
                                    productName.trim(),
                                    price,
                                    costPrice,
                                    supplier.ifBlank { null },
                                    stock,
                                    selectedCategory
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Producto")
                }
            }
        }
    }
}