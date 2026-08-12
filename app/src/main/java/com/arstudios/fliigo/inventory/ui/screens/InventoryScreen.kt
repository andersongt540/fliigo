// app/src/main/java/com/arstudios/fliigo/inventory/ui/screens/InventoryScreen.kt
package com.arstudios.fliigo.inventory.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.inventory.data.ProductDto
import com.arstudios.fliigo.inventory.ui.components.EditProductDialog
import com.arstudios.fliigo.inventory.ui.components.RegisterProductScreen
import com.arstudios.fliigo.inventory.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val products = uiState.products
    val categories = uiState.categories

    val fondoVerde = colorResource(R.color.fondo_verde)
    val amarilloHeader = colorResource(R.color.amarillo_header)
    val botonesOscuros = colorResource(R.color.botones_oscuros)
    val textoOscuro = colorResource(R.color.texto_oscuro)
    val grisFondo = colorResource(R.color.gris_fondo)
    val verdeExito = colorResource(R.color.verde_exito)

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showProductScreen by remember { mutableStateOf(false) }
    var categoryInput by remember { mutableStateOf("") }

    var productToEdit by remember { mutableStateOf<ProductDto?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(fondoVerde)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- ENCABEZADO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📦", fontSize = 20.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.inventory_title),
                        color = amarilloHeader,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.inventory_subtitle),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- CONTENEDOR PRINCIPAL ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = grisFondo)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.inventory_total_products),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = textoOscuro
                    )

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = botonesOscuros)
                        }
                    } else if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.inventory_empty_list),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(
                                items = products,
                                key = { it.id ?: it.name }
                            ) { product ->
                                val defaultCategory = stringResource(R.string.category_general)
                                val categoryName = product.category ?: defaultCategory

                                val density = LocalDensity.current
                                val maxRevealPx = with(density) { 140.dp.toPx() }
                                val offsetX = remember { Animatable(0f) }
                                val coroutineScope = rememberCoroutineScope()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    // --- BOTONES DE EDICIÓN Y ELIMINACIÓN ---
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
                                                productToEdit = product
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color.White, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = stringResource(R.string.btn_edit),
                                                tint = Color(0xFF1976D2),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch { offsetX.animateTo(0f) }
                                                product.id?.let { productId ->
                                                    viewModel.deleteProduct(productId)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color.White, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.btn_delete),
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // --- TARJETA PRINCIPAL DESLIZABLE ---
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
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = product.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = textoOscuro
                                                )
                                                Text(
                                                    text = stringResource(R.string.category_label, categoryName),
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = stringResource(R.string.currency_format, String.format(Locale.US, "%.2f", product.price)),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = verdeExito
                                                )
                                                Text(
                                                    text = stringResource(R.string.stock_label, product.stock),
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DIÁLOGO DE EDICIÓN ---
        if (productToEdit != null) {
            EditProductDialog(
                product = productToEdit!!,
                categories = categories,
                onDismiss = { productToEdit = null },
                onSave = { name, price, costPrice, provider, stock, category ->
                    productToEdit?.id?.let { id ->
                        viewModel.updateProduct(
                            productId = id,
                            name = name,
                            price = price,
                            costPrice = costPrice,
                            provider = provider,
                            stock = stock,
                            category = category
                        ) {
                            productToEdit = null
                        }
                    }
                }
            )
        }

        // --- BOTONES FLOTANTES (FABs) ---
        if (!showProductScreen) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = {
                        categoryInput = ""
                        showCategoryDialog = true
                    },
                    containerColor = amarilloHeader,
                    contentColor = textoOscuro,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = stringResource(R.string.cd_create_category)
                    )
                }

                FloatingActionButton(
                    onClick = { showProductScreen = true },
                    containerColor = botonesOscuros,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_register_product)
                    )
                }
            }
        }

        // --- DIÁLOGO: CREAR CATEGORÍA ---
        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                title = { Text(stringResource(R.string.dialog_create_category_title)) },
                text = {
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text(stringResource(R.string.dialog_category_hint)) },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (categoryInput.isNotBlank()) {
                            viewModel.createCategory(categoryInput.trim()) {
                                showCategoryDialog = false
                            }
                        }
                    }) {
                        Text(stringResource(R.string.btn_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCategoryDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }

        // --- PANTALLA COMPLETA: REGISTRAR PRODUCTO ---
        if (showProductScreen) {
            RegisterProductScreen(
                categories = categories,
                onBack = { showProductScreen = false },
                onSave = { name, price, costPrice, provider, stock, category ->
                    viewModel.registerProduct(
                        name = name,
                        price = price,
                        costPrice = costPrice,
                        provider = provider,
                        stock = stock,
                        category = category
                    ) {
                        showProductScreen = false
                    }
                }
            )
        }
    }
}