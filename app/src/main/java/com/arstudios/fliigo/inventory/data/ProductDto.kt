package com.arstudios.fliigo.inventory.data

data class CategoryDto(
    val id: Int? = null,
    val storeId: Int,
    val name: String
)

data class ProductDto(
    val id: Int? = null,
    val barcode: String? = null,  // Nuevo campo para código de barras manual o escaneado
    val storeId: Int,
    val name: String,
    val price: Double,
    val costPrice: Double? = 0.0,
    val provider: String? = "",
    val stock: Int,
    val category: String?
)
