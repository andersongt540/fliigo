package com.arstudios.fliigo.balance.data

data class GroupedSale(
    val clientName: String,
    val date: String, // El ID de grupo será la fecha exacta
    val address: String,
    val phone: String,
    val items: List<SaleItem>,
    val totalAmount: Double
)
