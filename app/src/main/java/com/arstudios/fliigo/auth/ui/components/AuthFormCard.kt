package com.arstudios.fliigo.auth.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arstudios.fliigo.R
import com.arstudios.fliigo.auth.viewmodel.AuthViewModel
import com.arstudios.fliigo.core.theme.BotonesOscuros
import com.arstudios.fliigo.core.theme.TextoOscuro

@Composable
fun AuthFormCard(
    viewModel: AuthViewModel,
    context: Context,
    onLoginSuccess: (hasStore: Boolean) -> Unit
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

            // --- CAMPO DE CORREO ---
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { newValue: String -> viewModel.email = newValue },
                label = { Text(stringResource(R.string.email_or_user_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- CAMPO DE CONTRASEÑA ---
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { newValue: String -> viewModel.password = newValue },
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
                    enabled = !(viewModel.isLoading as? Boolean ?: false)
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
                    enabled = !(viewModel.isLoading as? Boolean ?: false)
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