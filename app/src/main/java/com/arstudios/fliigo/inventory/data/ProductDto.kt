package com.arstudios.fliigo.inventory.data

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("storeId") val storeId: Int,
    @SerializedName("name") val name: String
)

data class ProductDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("storeId") val storeId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("costPrice") val costPrice: Double? = 0.0,
    @SerializedName("provider") val provider: String? = "",
    @SerializedName("stock") val stock: Int,
    @SerializedName("category") val category: String?
)
