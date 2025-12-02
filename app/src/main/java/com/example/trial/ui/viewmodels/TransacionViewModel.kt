package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.CategoriaEntity
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
    val errorMessage: String? = null,
    val isIncome: Boolean = false
)

@HiltViewModel
class TransaccionViewModel @Inject constructor(
    private val transaccionRepository: TransaccionRepository,
    private val metaAhorroRepository: MetaAhorroRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransaccionUiState())
    val uiState: StateFlow<TransaccionUiState> = _uiState.asStateFlow()
    //private val _todasLasCategorias = MutableStateFlow<List<CategoriaEntity>>(emptyList())

    private val ingreso = MutableStateFlow(
        CategoriaEntity(
            0,
            "Ingreso",
            ""))

    val _todasLasCategorias : StateFlow<List<CategoriaEntity>> =
        categoriaRepository.getAllCategorias()
            .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )


    val catFiltrado: StateFlow<List<CategoriaEntity>> =
        combine(_todasLasCategorias, ingreso) { lista, ingreso ->
            lista.filter { it.idCategoria != ingreso.idCategoria }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(categoryId: Int, categoryName: String) {
        _uiState.update { it.copy(categoryId = categoryId, categoryName = categoryName) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onIncomeToggle(isIncome: Boolean) {
        _uiState.update { it.copy(isIncome = isIncome) }

        if (isIncome) {
            // Si es ingreso, asignar automáticamente la categoría "Ingreso"
            val ingresoCategory = _todasLasCategorias.value.firstOrNull { it.nombre.lowercase() == "ingreso" }
            ingresoCategory?.let {
                onCategoryChange(it.idCategoria, it.nombre)
            }
        } else {
            // Si es gasto, resetear la categoría
            _uiState.update { it.copy(categoryId = 0, categoryName = "") }
        }
    }

    fun addTransaccion() {
        val currentState = _uiState.value
        val montoInput = currentState.amount.toDoubleOrNull()

        if (montoInput == null || montoInput <= 0) {
            _uiState.update { it.copy(errorMessage = "Monto inválido") }
            return
        }

        val monto = if (currentState.isIncome) montoInput else -montoInput

        if (currentState.categoryId == 0) {
            _uiState.update { it.copy(errorMessage = "Selecciona una categoría") }
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
                    idCuenta = 1
                )

                transaccionRepository.addTransaccion(transaccion)

                if (monto < 0) {
                    updateMetaProgress(currentState.categoryId, monto)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        showSuccess = true,
                        amount = "",
                        note = "",
                        categoryId = 0,
                        categoryName = "",
                        isIncome = false
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
                    monto = -amount,
                    idCategoria = categoryId,
                    descripcion = "Gasto rápido",
                    fecha = System.currentTimeMillis(),
                    idCuenta = 1
                )

                transaccionRepository.addTransaccion(transaccion)
                updateMetaProgress(categoryId, -amount)

                _uiState.update {
                    it.copy(
                        showSuccess = true,
                        amount = "",
                        note = "",
                        categoryId = 0,
                        categoryName = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    private suspend fun updateMetaProgress(categoryId: Int, amount: Double) {
        try {
            val meta = metaAhorroRepository.getActiveMetaByCategory(categoryId)
            if (meta != null) {
                val newAmount = meta.montoActual + amount
                metaAhorroRepository.actualizarMonto(meta.idMeta, newAmount)

                if (newAmount >= meta.monto) {
                    metaAhorroRepository.marcarComoCompletada(meta.idMeta)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

