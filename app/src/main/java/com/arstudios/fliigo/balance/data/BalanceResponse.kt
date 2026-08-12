package com.arstudios.fliigo.balance.data

data class BalanceResponse(
    val success: Boolean,
    val storeName: String?,
    val category: String?,
    val balance: Double?,
    val totalIncome: Double?,
    val totalExpenses: Double?,
    val error: String? = null,
    val sales: List<SaleItemDto>? = null
)

data class SaleItemDto(
    val id: Int?,
    val productName: String?,
    val clientName: String?,
    val amount: Double?,
    val date: String?,
    val address: String?,
    val phone: String?,
    val quantity: Int?
)