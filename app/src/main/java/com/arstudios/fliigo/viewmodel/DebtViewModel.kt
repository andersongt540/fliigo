package com.arstudios.fliigo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed class DebtUiState {
    object Loading : DebtUiState()
    data class Success(
        val storeName: String = "Tienda AR",
        val totalReceivable: Double = 0.0,
        val receivableClientsCount: Int = 0,
        val totalPayable: Double = 0.0,
        val payableClientsCount: Int = 0,
        val searchQuery: String = "",
        val debtList: List<DebtItem> = emptyList()
    ) : DebtUiState()
    data class Error(val message: String) : DebtUiState()
}

data class DebtItem(
    val id: String,
    val title: String,
    val timeAgo: String,
    val amount: Double,
    val paymentMethod: String
)

class DebtViewModel : ViewModel() {

    var uiState: DebtUiState by mutableStateOf(DebtUiState.Loading)
        private set

    init {
        loadDebtsData()
    }

    fun loadDebtsData() {
        viewModelScope.launch {
            uiState = DebtUiState.Loading
            try {
                // Simulación de carga de datos acorde al diseño
                kotlinx.coroutines.delay(500)
                uiState = DebtUiState.Success(
                    storeName = "Tienda AR",
                    totalReceivable = 0.0,
                    receivableClientsCount = 0,
                    totalPayable = 0.0,
                    payableClientsCount = 0,
                    debtList = listOf(
                        DebtItem("1", "Cobrar 1", "Hace 30 días", 0.0, "Pago Móvil"),
                        DebtItem("2", "Cobrar 1", "Hace 30 días", 0.0, "Pago Móvil"),
                        DebtItem("3", "Cobrar 1", "Hace 30 días", 0.0, "Pago Móvil"),
                        DebtItem("4", "Cobrar 1", "Hace 30 días", 0.0, "Pago Móvil")
                    )
                )
            } catch (e: Exception) {
                uiState = DebtUiState.Error(e.message ?: "Error al cargar las deudas")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        val currentState = uiState
        if (currentState is DebtUiState.Success) {
            uiState = currentState.copy(searchQuery = query)
        }
    }
}