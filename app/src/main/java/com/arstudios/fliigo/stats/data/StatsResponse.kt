package com.arstudios.fliigo.stats.data

data class StatsResponse(
    val totalSalesMonth: Double,
    val grossUtility: Double,
    val topClients: List<TopClientDto> = emptyList()
)

data class TopClientDto(
    val name: String,
    val purchases: Int
)
