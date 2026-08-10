package com.arstudios.fliigo.data.model

data class CategoryDto(
    val id: Int? = null,
    val storeId: Int,
    val name: String
)

data class ProductDto(
    val id: Int? = null,
    val storeId: Int,
    val name: String,
    val price: Double,          // Precio de venta
    val costPrice: Double? = 0.0, // Precio de costo
    val provider: String? = "",   // Proveedor opcional
    val stock: Int,
    val category: String?       // Nombre o ID de la categoría asociada
)