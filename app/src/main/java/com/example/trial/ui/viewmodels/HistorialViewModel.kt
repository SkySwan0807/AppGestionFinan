package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


data class FiltrosUI(
    val selectedCategory: Int = 0,
    val selectedTipo: String = "",
    val montoMin: String = "",
    val montoMax: String = "",
    val fechaFiltro: String = ""
)

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val transaccionRepository: TransaccionRepository
) : ViewModel() {

    private val _filtros = MutableStateFlow(FiltrosUI())

    // Flow con TODAS las transacciones
    val _historial: StateFlow<List<TransaccionEntity>> =
        transaccionRepository.getAllTransacciones() // Debes tener este DAO/Repo
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val historialFiltrado: StateFlow<List<TransaccionEntity>> =
        combine(_historial, _filtros) { lista, filtros ->
            lista.filter { trans ->
                filtros.selectedCategory == 0 || trans.idCategoria == filtros.selectedCategory
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setFiltroCategoria(categoriaId: Int) {
        _filtros.value = _filtros.value.copy(selectedCategory = categoriaId)
    }

    fun onDelete(transaccion: TransaccionEntity) {
        viewModelScope.launch {
            transaccionRepository.deleteTransaccion(transaccion)
        }
    }

    fun onEdit(transaccion: TransaccionEntity) {
        viewModelScope.launch {
            transaccionRepository.updateTransaccion(transaccion)
        }
    }
}
