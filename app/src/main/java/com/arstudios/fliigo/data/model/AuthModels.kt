package com.arstudios.fliigo.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegistroInitRequest(
    val email: String,
    val password: String
)

data class RegistroVerifyRequest(
    val email: String,
    val code: String
)

data class UsuarioResponse(
    val success: Boolean,
    val message: String? = null,
    val userId: Int? = null,
    val storeId: Int? = null, // <--- Añadido para coincidir con SetupStoreViewModel
    val hasStore: Boolean? = null,
    val token: String? = null,
    val error: String? = null,
    val mensaje: String? = null
)