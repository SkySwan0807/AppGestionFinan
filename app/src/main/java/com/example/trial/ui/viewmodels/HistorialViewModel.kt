package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val transaccionRepository: TransaccionRepository
) : ViewModel() {

    // Flow con TODAS las transacciones
    val historial: StateFlow<List<TransaccionEntity>> =
        transaccionRepository.getAllTransacciones() // Debes tener este DAO/Repo
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
}
