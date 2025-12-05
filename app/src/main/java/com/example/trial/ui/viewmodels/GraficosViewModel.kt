package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.dao.CategorySum
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject


data class CategorySumFecha(
    val idCategoria: Int,
    val total: Double,
    val fecha: Long?
)

@HiltViewModel
class GraficosViewModel @Inject constructor(
    private val transaccionRepository: TransaccionRepository
) : ViewModel() {


    val gastoDeCategorias: StateFlow<List<CategorySum>> =
        transaccionRepository.getSumByCategory()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val ingresosTotales: StateFlow<List<CategorySumFecha>?> =
        transaccionRepository.getTotalPorCategoria(6)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )





}
