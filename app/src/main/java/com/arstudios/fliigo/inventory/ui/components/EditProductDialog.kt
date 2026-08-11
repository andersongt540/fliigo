package com.arstudios.fliigo.inventory.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.inventory.data.CategoryDto
import com.arstudios.fliigo.inventory.data.ProductDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: ProductDto,
    categories: List<CategoryDto>,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, costPrice: Double?, provider: String?, stock: Int, category: String?) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var costPriceStr by remember { mutableStateOf(product.costPrice?.toString() ?: "") }
    var provider by remember { mutableStateOf(product.provider ?: "") }
    var stockStr by remember { mutableStateOf(product.stock.toString()) }
    var selectedCategory by remember { mutableStateOf(product.category) }
    var expandedCategory by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Precio ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("Costo ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = provider,
                        onValueChange = { provider = it },
                        label = { Text("Proveedor") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory ?: "Seleccionar Categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    expandedCategory = false
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = priceStr.toDoubleOrNull()
                val costPrice = costPriceStr.toDoubleOrNull()
                val stock = stockStr.toIntOrNull()

                when {
                    name.isBlank() -> errorMessage = "Ingresa un nombre válido"
                    price == null || price <= 0.0 -> errorMessage = "Precio inválido"
                    stock == null || stock < 0 -> errorMessage = "Stock inválido"
                    else -> {
                        onSave(
                            name.trim(),
                            price,
                            costPrice,
                            provider.ifBlank { null },
                            stock,
                            selectedCategory
                        )
                    }
                }
            }) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}