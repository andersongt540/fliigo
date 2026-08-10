package com.arstudios.fliigo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.ui.theme.*
import com.arstudios.fliigo.viewmodel.SetupStoreViewModel

@Composable
fun SetupStoreScreen(
    viewModel: SetupStoreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onStoreCreated: () -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var storeCategory by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoVerde)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- SECCIÓN SUPERIOR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .background(
                        AmarilloHeader,
                        shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.setup_store_title),
                        color = TextoOscuro,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.setup_store_subtitle),
                        color = TextoOscuro,
                        fontSize = 14.sp
                    )
                }
            }

            // --- SECCIÓN INFERIOR (FORMULARIO) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.75f)
                    .padding(horizontal = 24.dp)
                    .offset(y = (-24.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.store_details_header),
                            color = TextoOscuro,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Nombre del local
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text(stringResource(R.string.store_name_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rubro
                        OutlinedTextField(
                            value = storeCategory,
                            onValueChange = { storeCategory = it },
                            label = { Text(stringResource(R.string.store_category_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dirección
                        OutlinedTextField(
                            value = storeAddress,
                            onValueChange = { storeAddress = it },
                            label = { Text(stringResource(R.string.store_address_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Teléfono del local
                        OutlinedTextField(
                            value = storePhone,
                            onValueChange = { storePhone = it },
                            label = { Text(stringResource(R.string.store_phone_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón para finalizar y crear tienda
                        Button(
                            onClick = {
                                viewModel.registerStore(
                                    context = context.applicationContext, // Uso seguro de applicationContext
                                    storeName = storeName,
                                    category = storeCategory,
                                    address = storeAddress,
                                    phone = storePhone,
                                    onSuccess = onStoreCreated
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BotonesOscuros),
                            enabled = !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.btn_save_store),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Mostrar mensajes de error si ocurren
                        if (!viewModel.errorMessage.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = viewModel.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}