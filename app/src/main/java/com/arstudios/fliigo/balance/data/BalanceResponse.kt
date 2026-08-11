package com.arstudios.fliigo.balance.data

data class BalanceResponse(
    val success: Boolean,
    val storeName: String?,
    val category: String?,
    val balance: Double?,
    val totalIncome: Double?,
    val totalExpenses: Double?,
    val error: String? = null,
    val sales: List<SaleItemDto>? = null // <--- Lista de ventas de la base de datos
)

data class SaleItemDto(
    val productName: String?,
    val clientName: String?,
    val amount: Double?
)