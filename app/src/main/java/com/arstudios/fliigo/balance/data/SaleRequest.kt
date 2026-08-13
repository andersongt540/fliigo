package com.arstudios.fliigo.balance.data

import com.google.gson.annotations.SerializedName

data class SaleRequest(
    @SerializedName("storeId") val storeId: Int,
    @SerializedName("clientName") val clientName: String,
    val address: String,
    val phone: String,
    val items: List<SaleItemRequest> // Lista de productos
)

data class SaleItemRequest(
    val productId: Int,
    val quantity: Int
)

data class SaleResponse(
    val success: Boolean? = true,
    val message: String? = null,
    val saleId: Int? = null,
    val error: String? = null
)
