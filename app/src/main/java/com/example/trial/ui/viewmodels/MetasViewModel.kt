package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.MetaAhorroEntity
import com.example.trial.data.repository.MetaAhorroRepository
import com.example.trial.data.repository.TransaccionRepository
import com.example.trial.data.repository.CategoriaRepository
import com.example.trial.data.local.entities.CategoriaEntity
import com.example.trial.notifications.GoalNotificationManager
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

enum class MetaFilter {
    TODAS, ACTIVAS, COMPLETADAS, CANCELADAS, NO_CUMPLIDAS, VIGENTES
}

enum class MetaSortField {
    FECHA_OBJETIVO, PROGRESO, MONTO
}

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
    val errorMessage: String? = null,
    val editingMetaId: Int? = null,
    val isEditMode: Boolean = false,
    val selectedFilter: MetaFilter = MetaFilter.ACTIVAS,
    val sortBy: MetaSortField = MetaSortField.FECHA_OBJETIVO,
    val showDeleteConfirmation: Boolean = false,
    val metaToDelete: MetaAhorroEntity? = null
)

@HiltViewModel
class MetaAhorroViewModel @Inject constructor(
    private val metaRepository: MetaAhorroRepository,
    private val transaccionRepository: TransaccionRepository,
    private val categoriaRepository: CategoriaRepository,
    private val notificationManager: GoalNotificationManager
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
    // CARGA DE METAS CON PROGRESO (CON FILTROS)
    // -------------------------------------------------------------
    private fun loadMetas() {
        viewModelScope.launch {
            val filter = _uiState.value.selectedFilter
            val sortField = _uiState.value.sortBy
            
            // Seleccionar flujo según filtro
            val metasFlow = when (filter) {
                MetaFilter.ACTIVAS -> metaRepository.getActiveMetas()
                MetaFilter.COMPLETADAS -> metaRepository.getByEstado(2)
                MetaFilter.CANCELADAS -> metaRepository.getByEstado(3)
                MetaFilter.NO_CUMPLIDAS -> metaRepository.getByEstado(4)
                MetaFilter.VIGENTES -> metaRepository.getMetasVigentes(System.currentTimeMillis())
                MetaFilter.TODAS -> metaRepository.getAll()
            }
            
            metasFlow
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
                .map { metasWithProgress ->
                    // Aplicar ordenamiento
                    when (sortField) {
                        MetaSortField.FECHA_OBJETIVO -> 
                            metasWithProgress.sortedBy { it.meta.fechaObjetivo }
                        MetaSortField.PROGRESO -> 
                            metasWithProgress.sortedByDescending { it.progress }
                        MetaSortField.MONTO -> 
                            metasWithProgress.sortedByDescending { it.meta.monto }
                    }
                }
                .collect { metasWithProgress ->
                    // Enviar notificaciones basadas en el progreso
                    metasWithProgress.forEach { metaWithProgress ->
                        notificationManager.checkAndSendNotifications(metaWithProgress)
                    }
                    
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
    // ELIMINAR META
    // -------------------------------------------------------------
    fun deleteMeta(meta: MetaAhorroEntity) {
        viewModelScope.launch {
            try {
                metaRepository.delete(meta)
                notificationManager.clearNotificationHistory(meta.idMeta)
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

    // -------------------------------------------------------------
    // EDITAR META
    // -------------------------------------------------------------
    fun startEditing(meta: MetaAhorroEntity) {
        val monthsDiff = calculateMonthsDifference(
            meta.fechaInicio,
            meta.fechaObjetivo
        )

        _uiState.update {
            it.copy(
                isEditMode = true,
                editingMetaId = meta.idMeta,
                categoryId = meta.idCategoria,
                targetAmount = meta.monto.toString(),
                months = monthsDiff.toString(),
                description = meta.descripcion ?: ""
            )
        }
    }

    fun cancelEditing() {
        _uiState.update {
            it.copy(
                isEditMode = false,
                editingMetaId = null,
                categoryId = 0,
                targetAmount = "",
                months = "1",
                description = ""
            )
        }
    }

    private fun calculateMonthsDifference(start: Long, end: Long): Int {
        val diff = end - start
        return (diff / (1000L * 60 * 60 * 24 * 30)).toInt().coerceAtLeast(1)
    }

    // -------------------------------------------------------------
    // ACTUALIZAR META (CREAR O EDITAR)
    // -------------------------------------------------------------
    fun saveMeta() {
        val current = _uiState.value

        val amount = current.targetAmount.toDoubleOrNull()
        val months = current.months.toIntOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Monto debe ser mayor a 0") }
            return
        }

        if (months == null || months <= 0) {
            _uiState.update { it.copy(errorMessage = "Plazo debe ser mayor a 0") }
            return
        }

        if (current.categoryId == 0) {
            _uiState.update { it.copy(errorMessage = "Debe seleccionar una categoría") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                if (current.isEditMode && current.editingMetaId != null) {
                    // MODO EDICIÓN
                    val metaExistente = metaRepository.getById(current.editingMetaId)
                    metaExistente?.let { meta ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = meta.fechaInicio
                        calendar.add(Calendar.MONTH, months)
                        val nuevaFechaObjetivo = calendar.timeInMillis

                        val metaActualizada = meta.copy(
                            idCategoria = current.categoryId,
                            monto = amount,
                            descripcion = current.description.takeIf { it.isNotBlank() },
                            fechaObjetivo = nuevaFechaObjetivo
                        )
                        metaRepository.update(metaActualizada)
                    }
                } else {
                    // MODO CREACIÓN
                    val calendar = Calendar.getInstance()
                    val start = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, months)
                    val end = calendar.timeInMillis

                    val categoriaNombre = current.categories
                        .firstOrNull { it.idCategoria == current.categoryId }?.nombre ?: "Meta"

                    val meta = MetaAhorroEntity(
                        idCategoria = current.categoryId,
                        monto = amount,
                        montoActual = 0.0,
                        fechaInicio = start,
                        fechaObjetivo = end,
                        descripcion = current.description.takeIf { it.isNotBlank() },
                        idEstado = 1, // Activo
                        nombre = categoriaNombre
                    )

                    metaRepository.insert(meta)
                }

                _uiState.update {
                    it.copy(
                        showSuccess = true,
                        isEditMode = false,
                        editingMetaId = null,
                        isLoading = false,
                        categoryId = 0,
                        targetAmount = "",
                        months = "1",
                        description = ""
                    )
                }

                loadMetas()

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

    // -------------------------------------------------------------
    // CAMBIAR ESTADO DE META
    // -------------------------------------------------------------
    fun markAsCompleted(meta: MetaAhorroEntity) {
        viewModelScope.launch {
            try {
                metaRepository.update(meta.copy(idEstado = 2)) // Completado
                loadMetas()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    fun markAsCancelled(meta: MetaAhorroEntity) {
        viewModelScope.launch {
            try {
                metaRepository.update(meta.copy(idEstado = 3)) // Cancelado
                loadMetas()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    // -------------------------------------------------------------
    // CONFIRMACIÓN DE ELIMINACIÓN
    // -------------------------------------------------------------
    fun showDeleteConfirmation(meta: MetaAhorroEntity) {
        _uiState.update {
            it.copy(
                showDeleteConfirmation = true,
                metaToDelete = meta
            )
        }
    }

    fun hideDeleteConfirmation() {
        _uiState.update {
            it.copy(
                showDeleteConfirmation = false,
                metaToDelete = null
            )
        }
    }

    fun confirmDelete() {
        val meta = _uiState.value.metaToDelete
        if (meta != null) {
            deleteMeta(meta)
            hideDeleteConfirmation()
        }
    }

    // -------------------------------------------------------------
    // FILTROS Y ORDENAMIENTO
    // -------------------------------------------------------------
    fun setFilter(filter: MetaFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadMetas()
    }

    fun setSortField(sortField: MetaSortField) {
        _uiState.update { it.copy(sortBy = sortField) }
        loadMetas()
    }

    // -------------------------------------------------------------
    // LIMPIEZA AL DESTRUIR EL VIEWMODEL
    // -------------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
        // Limpiar historial de notificaciones cuando se destruye el ViewModel
        notificationManager.clearAllNotificationHistory()
    }
}
