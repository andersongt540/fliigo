package com.arstudios.fliigo.data.model

data class SaleRequest(
    val storeId: Int,
    val clientName: String,
    val address: String,
    val phone: String,
    val productId: Int,
    val quantity: Int
)

data class SaleResponse(
    val success: Boolean? = true,
    val message: String? = null,
    val sale: Any? = null,
    val error: String? = null
)