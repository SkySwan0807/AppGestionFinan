package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.MetaAhorroEntity
import com.example.trial.data.repository.MetaAhorroRepository
import com.example.trial.data.repository.TransaccionRepository
import com.example.trial.data.repository.CategoriaRepository
import com.example.trial.data.local.entities.CategoriaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class MetaAhorroWithProgress(
    val meta: MetaAhorroEntity,
    val progress: Float,
    val remainingAmount: Double,
    val remainingDays: Int
)

data class MetaAhorroUiState(
    val metas: List<MetaAhorroWithProgress> = emptyList(),
    val categories: List<CategoriaEntity> = emptyList(),
    val categoryId: Int = 0,
    val categoryName: String = "",
    val targetAmount: String = "",
    val months: String = "1",
    val description: String = "",
    val isLoading: Boolean = false,
    val showSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MetaAhorroViewModel @Inject constructor(
    private val metaRepository: MetaAhorroRepository,
    private val transaccionRepository: TransaccionRepository,
    private val categoriaRepository: CategoriaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetaAhorroUiState())
    val uiState: StateFlow<MetaAhorroUiState> = _uiState.asStateFlow()

    init {
        loadCategorias()
        loadMetas()
    }

    val categorias = categoriaRepository.getAllCategorias()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // -------------------------------------------------------------
    // CARGA DE CATEGORÍAS DESDE LA BASE DE DATOS
    // -------------------------------------------------------------
    private fun loadCategorias() {
        viewModelScope.launch {
            categoriaRepository.getAllCategorias()
                .collect { categorias ->
                    _uiState.update { it.copy(categories = categorias) }
                }
        }
    }

    // -------------------------------------------------------------
    // CARGA DE METAS CON PROGRESO
    // -------------------------------------------------------------
    private fun loadMetas() {
        viewModelScope.launch {
            metaRepository.getActiveMetas()
                .flatMapLatest { metas ->
                    combine(
                        metas.map { meta ->
                            transaccionRepository.getTotalPorCuenta(meta.idCategoria)
                                .map { total ->
                                    val currentSpent = total ?: 0.0

                                    val progress = if (meta.monto > 0) {
                                        (currentSpent / meta.monto)
                                            .coerceIn(0.0, 1.0)
                                            .toFloat()
                                    } else 0f

                                    MetaAhorroWithProgress(
                                        meta = meta.copy(montoActual = currentSpent),
                                        progress = progress,
                                        remainingAmount = (meta.monto - currentSpent).coerceAtLeast(0.0),
                                        remainingDays = calculateRemainingDays(meta.fechaObjetivo)
                                    )
                                }
                        }
                    ) { it.toList() }
                }
                .collect { metasWithProgress ->
                    _uiState.update {
                        it.copy(metas = metasWithProgress, isLoading = false)
                    }
                }
        }
    }

    // -------------------------------------------------------------
    // CAMBIO DE CAMPOS DEL FORMULARIO
    // -------------------------------------------------------------
    fun onCategoryChange(id: Int) {
        _uiState.update { it.copy(categoryId = id) }
    }

    fun onTargetAmountChange(amount: String) {
        _uiState.update { it.copy(targetAmount = amount) }
    }

    fun onMonthsChange(months: String) {
        _uiState.update { it.copy(months = months) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    // -------------------------------------------------------------
    // CREAR NUEVA META
    // -------------------------------------------------------------
    fun createMeta() {
        val current = _uiState.value

        val amount = current.targetAmount.toDoubleOrNull()
        val months = current.months.toIntOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Monto inválido") }
            return
        }

        if (months == null || months <= 0) {
            _uiState.update { it.copy(errorMessage = "Plazo inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val calendar = Calendar.getInstance()
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, months)
                val end = calendar.timeInMillis

                val meta = MetaAhorroEntity(
                    idCategoria = current.categoryId,
                    monto = amount,
                    montoActual = 0.0,
                    fechaInicio = start,
                    fechaObjetivo = end,
                    descripcion = current.description.takeIf { it.isNotBlank() } ?: "",
                    idEstado = 0,
                    nombre = current.categoryName
                )

                metaRepository.insert(meta)

                _uiState.update {
                    it.copy(
                        showSuccess = true,
                        categoryId = current.categoryId,
                        categoryName = current.categoryName
                    )
                }

                loadMetas()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al crear meta: ${e.message}"
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------
    // ELIMINAR META
    // -------------------------------------------------------------
    fun deleteMeta(meta: MetaAhorroEntity) {
        viewModelScope.launch {
            try {
                metaRepository.delete(meta)
                loadMetas()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al eliminar: ${e.message}") }
            }
        }
    }

    // -------------------------------------------------------------
    // UTILIDADES
    // -------------------------------------------------------------
    private fun calculateRemainingDays(endDate: Long): Int {
        val now = System.currentTimeMillis()
        val diff = endDate - now
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    fun clearSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
