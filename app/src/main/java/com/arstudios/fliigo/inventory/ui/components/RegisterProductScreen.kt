// app/src/main/java/com/arstudios/fliigo/balance/ui/components/RegisterSaleDialog.kt
package com.arstudios.fliigo.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.inventory.data.CategoryDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterProductScreen(
    categories: List<CategoryDto>,
    onBack: () -> Unit,
    onSave: (name: String, price: Double, costPrice: Double?, provider: String?, stock: Int, category: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var costPriceStr by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fondoVerde = colorResource(R.color.fondo_verde)
    val botonesOscuros = colorResource(R.color.botones_oscuros)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_product_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = fondoVerde)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.product_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text(stringResource(R.string.price_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = costPriceStr,
                    onValueChange = { costPriceStr = it },
                    label = { Text(stringResource(R.string.cost_price_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text(stringResource(R.string.plain_stock_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text(stringResource(R.string.provider_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
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
                        .menuAnchor()
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

            Spacer(modifier = Modifier.weight(1f))

            val errorValidName = stringResource(R.string.error_valid_name)
            val errorInvalidPrice = stringResource(R.string.error_invalid_price)
            val errorInvalidStock = stringResource(R.string.error_invalid_stock)

            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull()
                    val costPrice = costPriceStr.toDoubleOrNull()
                    val stock = stockStr.toIntOrNull()

                    when {
                        name.isBlank() -> errorMessage = errorValidName
                        price == null || price <= 0.0 -> errorMessage = errorInvalidPrice
                        stock == null || stock < 0 -> errorMessage = errorInvalidStock
                        else -> {
                            onSave(name.trim(), price, costPrice, provider.ifBlank { null }, stock, selectedCategory)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = botonesOscuros)
            ) {
                Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}
