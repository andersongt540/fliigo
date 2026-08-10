package com.arstudios.fliigo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.viewmodel.InventoryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val products = uiState.products

    val fondoVerde = colorResource(R.color.fondo_verde)
    val amarilloHeader = colorResource(R.color.amarillo_header)
    val textoOscuro = colorResource(R.color.texto_oscuro)
    val grisFondo = colorResource(R.color.gris_fondo)
    val verdeExito = colorResource(R.color.verde_exito)

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }

    var categoryInput by remember { mutableStateOf("") }
    var productNameInput by remember { mutableStateOf("") }
    var productPriceInput by remember { mutableStateOf("") }
    var productStockInput by remember { mutableStateOf("") }

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
                            CircularProgressIndicator(color = textoOscuro)
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(products) { product ->
                                val defaultCategory = stringResource(R.string.category_general)
                                val categoryName = product.category ?: defaultCategory

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                text = "$ ${String.format(Locale.US, "%.2f", product.price)}",
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

        // --- BOTONES FLOTANTES (FABs) ---
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
                onClick = {
                    productNameInput = ""
                    productPriceInput = ""
                    productStockInput = ""
                    showProductDialog = true
                },
                containerColor = verdeExito,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_register_product)
                )
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

        // --- DIÁLOGO: REGISTRAR PRODUCTO ---
        if (showProductDialog) {
            AlertDialog(
                onDismissRequest = { showProductDialog = false },
                title = { Text(stringResource(R.string.dialog_register_product_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = productNameInput,
                            onValueChange = { productNameInput = it },
                            label = { Text(stringResource(R.string.dialog_product_name_hint)) },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = productPriceInput,
                            onValueChange = { productPriceInput = it },
                            label = { Text(stringResource(R.string.dialog_product_price_hint)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = productStockInput,
                            onValueChange = { productStockInput = it },
                            label = { Text(stringResource(R.string.dialog_product_stock_hint)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val price = productPriceInput.toDoubleOrNull() ?: 0.0
                        val stock = productStockInput.toIntOrNull() ?: 0
                        if (productNameInput.isNotBlank()) {
                            viewModel.registerProduct(
                                name = productNameInput.trim(),
                                price = price,
                                stock = stock,
                                category = null
                            ) {
                                showProductDialog = false
                            }
                        }
                    }) {
                        Text(stringResource(R.string.btn_register))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProductDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }
    }
}