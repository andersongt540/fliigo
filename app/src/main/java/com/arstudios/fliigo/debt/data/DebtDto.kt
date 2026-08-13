package com.arstudios.fliigo.debt.data

import com.google.gson.annotations.SerializedName

data class DebtDto(
    val id: Int? = null,
    @SerializedName("store_id") val storeId: Int? = null,
    @SerializedName("clientName") val clientName: String? = null,
    val amount: Double? = 0.0,
    val description: String? = null,
    val phone: String? = null, // Nuevo campo para WhatsApp
    val type: String? = "receivable",
    @SerializedName("isPaid") val isPaid: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class ClientSuggestionDto(
    val clientName: String,
    val phone: String?
)
