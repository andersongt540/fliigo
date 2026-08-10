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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.ui.theme.*
import com.arstudios.fliigo.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onLoginSuccess: (hasStore: Boolean) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoVerde)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- SECCIÓN SUPERIOR (FONDO AMARILLO Y LOGO) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.38f)
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
                        text = stringResource(R.string.welcome_title),
                        color = TextoOscuro,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LOGOTIPO",
                        color = TextoOscuro,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }
            }

            // --- SECCIÓN INFERIOR (TARJETA BLANCA CON FORMULARIO) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.62f)
                    .padding(horizontal = 24.dp)
                    .offset(y = (-32.dp)),
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
                            text = when (viewModel.currentView) {
                                "register" -> stringResource(R.string.register_card_title)
                                else -> stringResource(R.string.login_card_title)
                            },
                            color = TextoOscuro,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- CAMPOS DE CORREO Y CONTRASEÑA ---
                        OutlinedTextField(
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            label = { Text(stringResource(R.string.email_or_user_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = viewModel.password,
                            onValueChange = { viewModel.password = it },
                            label = { Text(stringResource(R.string.password_label)) },
                            visualTransformation = if (viewModel.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.passwordVisible = !viewModel.passwordVisible }) {
                                    Text(text = if (viewModel.passwordVisible) "👁️" else "🙈", fontSize = 14.sp)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (viewModel.currentView == "login") {
                            Button(
                                onClick = { viewModel.login(context, onLoginSuccess) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonesOscuros),
                                enabled = !viewModel.isLoading
                            ) {
                                if (viewModel.isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = stringResource(R.string.btn_login),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.currentView = "register"
                                    viewModel.mensajeEstado = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BotonesOscuros)
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_register),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.register(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BotonesOscuros),
                                enabled = !viewModel.isLoading
                            ) {
                                if (viewModel.isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = stringResource(R.string.btn_continue),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(onClick = {
                                viewModel.currentView = "login"
                                viewModel.mensajeEstado = ""
                            }) {
                                Text(
                                    text = stringResource(R.string.go_to_login),
                                    color = TextoOscuro,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (viewModel.mensajeEstado.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = viewModel.mensajeEstado,
                                color = TextoOscuro,
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