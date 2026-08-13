package com.arstudios.fliigo.debt.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arstudios.fliigo.core.network.RetrofitClient
import com.arstudios.fliigo.debt.data.ClientSuggestionDto
import com.arstudios.fliigo.debt.data.DebtDto
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
        val debtList: List<DebtDto> = emptyList(),
        val clientSuggestions: List<ClientSuggestionDto> = emptyList()
    ) : DebtUiState()
    data class Error(val message: String) : DebtUiState()
}

class DebtViewModel(application: Application) : AndroidViewModel(application) {

    var uiState: DebtUiState by mutableStateOf(DebtUiState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        loadDebtsData()
        loadClientSuggestions()
    }

    fun loadDebtsData(refreshing: Boolean = false) {
        viewModelScope.launch {
            if (refreshing) isRefreshing = true else uiState = DebtUiState.Loading
            
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId == -1) {
                    uiState = DebtUiState.Error("Tienda no identificada")
                    return@launch
                }

                val response = RetrofitClient.instance.getDebtsByStore(storeId)

                if (response.isSuccessful && response.body() != null) {
                    val allDebtsFromDb = response.body()!!
                    val pendingDebts = allDebtsFromDb.filter { !it.isPaid }
                    
                    val receivables = pendingDebts.filter { it.type == "receivable" }
                    val payables = pendingDebts.filter { it.type == "payable" }

                    val suggestions = (uiState as? DebtUiState.Success)?.clientSuggestions ?: emptyList()

                    uiState = DebtUiState.Success(
                        storeName = "Tienda AR",
                        totalReceivable = receivables.sumOf { it.amount ?: 0.0 },
                        receivableClientsCount = receivables.mapNotNull { it.clientName }.distinct().size,
                        totalPayable = payables.sumOf { it.amount ?: 0.0 },
                        payableClientsCount = payables.mapNotNull { it.clientName }.distinct().size,
                        debtList = pendingDebts,
                        clientSuggestions = suggestions
                    )
                } else {
                    uiState = DebtUiState.Error("Error al cargar deudas del servidor")
                }
            } catch (e: Exception) {
                uiState = DebtUiState.Error(e.message ?: "Error de red")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun loadClientSuggestions() {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)
                if (storeId != -1) {
                    val response = RetrofitClient.instance.getUniqueClients(storeId)
                    if (response.isSuccessful && response.body() != null) {
                        val currentState = uiState
                        if (currentState is DebtUiState.Success) {
                            uiState = currentState.copy(clientSuggestions = response.body()!!)
                        }
                    }
                }
            } catch (e: Exception) { /* Silencio */ }
        }
    }

    fun registerDebt(client: String, amount: Double, description: String, phone: String?, type: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("FliigoPrefs", Context.MODE_PRIVATE)
                val storeId = prefs.getInt("STORE_ID", -1)

                if (storeId != -1) {
                    val newDebt = DebtDto(
                        storeId = storeId,
                        clientName = client,
                        amount = amount,
                        description = description,
                        phone = phone,
                        type = type
                    )
                    val response = RetrofitClient.instance.registerDebt(newDebt)
                    if (response.isSuccessful) {
                        loadDebtsData()
                        onSuccess()
                    } else {
                        onError("No se pudo guardar en el servidor")
                    }
                } else {
                    onError("Tienda no identificada")
                }
            } catch (e: Exception) {
                onError("Error de red: ${e.localizedMessage}")
            }
        }
    }

    fun markAsPaid(debtId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.markDebtAsPaid(debtId)
                if (response.isSuccessful) {
                    loadDebtsData()
                    onSuccess()
                }
            } catch (e: Exception) { /* Error */ }
        }
    }

    fun deleteDebt(debtId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteDebt(debtId)
                if (response.isSuccessful) {
                    loadDebtsData()
                    onSuccess()
                }
            } catch (e: Exception) { /* Error */ }
        }
    }

    fun updateSearchQuery(query: String) {
        val currentState = uiState
        if (currentState is DebtUiState.Success) {
            uiState = currentState.copy(searchQuery = query)
        }
    }
}
