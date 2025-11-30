package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.data.repository.CategoriaRepository
import com.example.trial.data.repository.MetaAhorroRepository
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransaccionUiState(
    val amount: String = "",
    val categoryId: Int = 0,
    val categoryName: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val showSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TransaccionViewModel @Inject constructor(
    private val transaccionRepository: TransaccionRepository,
    private val metaAhorroRepository: MetaAhorroRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransaccionUiState())
    val uiState: StateFlow<TransaccionUiState> = _uiState.asStateFlow()

    private val _categorias = MutableStateFlow<List<com.example.trial.data.local.entities.CategoriaEntity>>(emptyList())
    val categorias: StateFlow<List<com.example.trial.data.local.entities.CategoriaEntity>> = _categorias.asStateFlow()

    init {
        loadCategorias()
    }

    private fun loadCategorias() {
        viewModelScope.launch {
            categoriaRepository.getAllCategorias()
                .collect { list ->
                    _categorias.value = list
                }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(categoryId: Int, categoryName: String) {
        _uiState.update { it.copy(categoryId = categoryId, categoryName = categoryName) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun addTransaccion() {
        val currentState = _uiState.value
        val monto = currentState.amount.toDoubleOrNull()

        if (monto == null || monto <= 0) {
            _uiState.update { it.copy(errorMessage = "Monto inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val transaccion = TransaccionEntity(
                    monto = monto,
                    idCategoria = currentState.categoryId,
                    descripcion = currentState.note.ifBlank { null },
                    fecha = System.currentTimeMillis(),
                    idCuenta = 0
                )

                transaccionRepository.addTransaccion(transaccion)

                // Actualizar progreso de meta si existe
                updateMetaProgress(currentState.categoryId, monto)

                _uiState.update {
                    TransaccionUiState(
                        showSuccess = true,
                        categoryId = currentState.categoryId,
                        categoryName = currentState.categoryName
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al guardar: ${e.message}"
                    )
                }
            }
        }
    }

    fun addQuickTransaccion(amount: Double, categoryId: Int) {
        viewModelScope.launch {
            try {
                val transaccion = TransaccionEntity(
                    monto = amount,
                    idCategoria = categoryId,
                    descripcion = "Transacción rápida",
                    fecha = System.currentTimeMillis(),
                    idCuenta = 0
                )

                transaccionRepository.addTransaccion(transaccion)
                updateMetaProgress(categoryId, amount)

                _uiState.update { it.copy(showSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    fun getCategoryNameById(idCategoria: Int): String {
        return _categorias.value.firstOrNull { it.idCategoria == idCategoria }?.nombre ?: "Desconocido"
    }

    private suspend fun updateMetaProgress(categoryId: Int, amount: Double) {
        val meta = metaAhorroRepository.getActiveMetaByCategory(categoryId)
        if (meta != null) {
            val newAmount = meta.montoActual + amount
            metaAhorroRepository.actualizarMonto(meta.idMeta, newAmount)

            if (newAmount >= meta.monto) {
                metaAhorroRepository.marcarComoCompletada(meta.idMeta)
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
