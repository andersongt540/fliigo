package com.arstudios.fliigo.balance.data

data class SaleItem(
    val id: Int,
    val productName: String,
    val clientName: String,
    val amount: Double,
    val date: String,
    val address: String,
    val phone: String,
    val quantity: Int,
    val localDay: String = ""
)