package com.arstudios.fliigo.data.model

data class StoreSetupRequest(
    val userId: Int,
    val storeName: String,
    val category: String,
    val address: String,
    val phone: String
)