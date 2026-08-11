package com.arstudios.fliigo.auth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arstudios.fliigo.R
import com.arstudios.fliigo.auth.ui.components.AuthFormCard
import com.arstudios.fliigo.auth.viewmodel.AuthViewModel
import com.arstudios.fliigo.core.theme.AmarilloHeader
import com.arstudios.fliigo.core.theme.FondoVerde
import com.arstudios.fliigo.core.theme.TextoOscuro

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
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
                AuthFormCard(
                    viewModel = viewModel,
                    context = context,
                    onLoginSuccess = onLoginSuccess
                )
            }
        }
    }
}