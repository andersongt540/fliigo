package com.arstudios.fliigo.SetupStore.data

data class StoreSetupRequest(
    val userId: Int,
    val storeName: String,
    val category: String,
    val address: String,
    val phone: String
)