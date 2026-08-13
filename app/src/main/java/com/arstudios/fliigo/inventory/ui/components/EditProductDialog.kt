package com.arstudios.fliigo.inventory.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.inventory.data.CategoryDto
import com.arstudios.fliigo.inventory.data.ProductDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: ProductDto,
    categories: List<CategoryDto>,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, costPrice: Double?, provider: String?, stock: Int, category: String?, barcode: String?) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var costPriceStr by remember { mutableStateOf(product.costPrice?.toString() ?: "") }
    var provider by remember { mutableStateOf(product.provider ?: "") }
    var stockStr by remember { mutableStateOf(product.stock.toString()) }
    var barcode by remember { mutableStateOf(product.barcode ?: "") }
    var selectedCategory by remember { mutableStateOf(product.category) }
    var expandedCategory by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_product_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.product_name_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text(stringResource(R.string.label_product_id)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escanee o ingrese código") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text(stringResource(R.string.price_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text(stringResource(R.string.cost_price_label)) },
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
                        label = { Text(stringResource(R.string.plain_stock_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = provider,
                        onValueChange = { provider = it },
                        label = { Text(stringResource(R.string.provider_label)) },
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
                        value = selectedCategory ?: stringResource(R.string.select_category_label),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.plain_category_label)) },
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
            val errorValidName = stringResource(R.string.error_valid_name)
            val errorInvalidPrice = stringResource(R.string.error_invalid_price)
            val errorInvalidStock = stringResource(R.string.error_invalid_stock)

            TextButton(onClick = {
                val price = priceStr.toDoubleOrNull()
                val costPrice = costPriceStr.toDoubleOrNull()
                val stock = stockStr.toIntOrNull()

                when {
                    name.isBlank() -> errorMessage = errorValidName
                    price == null || price <= 0.0 -> errorMessage = errorInvalidPrice
                    stock == null || stock < 0 -> errorMessage = errorInvalidStock
                    else -> {
                        onSave(
                            name.trim(),
                            price,
                            costPrice,
                            provider.ifBlank { null },
                            stock,
                            selectedCategory,
                            barcode.ifBlank { null }
                        )
                    }
                }
            }) {
                Text(stringResource(R.string.btn_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}