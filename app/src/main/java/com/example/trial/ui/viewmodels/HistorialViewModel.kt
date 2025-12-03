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
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.abs


data class FiltrosUI(
    val selectedCategory: Int = 0,
    val selectedTipo: String = "",
    val montoMin: String = "",
    val montoMax: String = "",
    val fechaInicio: Long? = null,
    val fechaFinal: Long? = null
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
            val montoMin = filtros.montoMin.toDoubleOrNull()
            val montoMax = filtros.montoMax.toDoubleOrNull()

            lista.filter { trans ->
                val montoAbs = abs(trans.monto)

                val cumpleCategoria = filtros.selectedCategory == 0 || trans.idCategoria == filtros.selectedCategory
                val cumpleMin = montoMin?.let { montoAbs >= it } ?: true
                val cumpleMax = montoMax?.let { montoAbs <= it } ?: true

                val cumpleFechaInicio = filtros.fechaInicio?.let { trans.fecha >= it } ?: true
                val cumpleFechaFinal  = filtros.fechaFinal?.let { trans.fecha <= it } ?: true

                cumpleCategoria && cumpleMin && cumpleMax && cumpleFechaInicio && cumpleFechaFinal
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun setFiltroCategoria(categoriaId: Int) {
        _filtros.value = _filtros.value.copy(selectedCategory = categoriaId)
    }

    fun setMontoMin(monto: String) {
        _filtros.value = _filtros.value.copy(montoMin = monto)
    }

    fun setMontoMax(monto: String) {
        _filtros.value = _filtros.value.copy(montoMax = monto)
    }

    fun setFechaMin(fechaMillis: Long?) {
        _filtros.value = _filtros.value.copy(fechaInicio = fechaMillis)
    }

    fun setFechaMax(fechaMillis: Long?) {
        _filtros.value = _filtros.value.copy(fechaFinal = fechaMillis)
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
