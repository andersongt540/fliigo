package com.arstudios.fliigo.balance.data

data class SaleItemRequest(
    val productId: Int,
    val quantity: Int
)

data class SaleRequest(
    val storeId: Int,
    val clientName: String,
    val address: String,
    val phone: String,
    val items: List<SaleItemRequest>
)

data class SaleResponse(
    val success: Boolean? = true,
    val message: String? = null,
    val sale: Any? = null,
    val error: String? = null
)
