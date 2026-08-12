package com.arstudios.fliigo.stats.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class StatsViewModel : ViewModel() {
    var totalSalesMonth by mutableStateOf(0.0)
    var grossUtility by mutableStateOf(0.0)
    
    init {
        // Cargar estadísticas
    }
}
