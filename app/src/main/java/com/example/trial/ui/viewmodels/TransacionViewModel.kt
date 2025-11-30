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

    // Flujo para categorías filtradas (sin "Ingreso" para el selector)
    private val _categoriasFiltradas = MutableStateFlow<List<com.example.trial.data.local.entities.CategoriaEntity>>(emptyList())
    val categoriasFiltradas: StateFlow<List<com.example.trial.data.local.entities.CategoriaEntity>> = _categoriasFiltradas.asStateFlow()

    // Flujo para TODAS las categorías (incluyendo "Ingreso")
    private val _todasLasCategorias = MutableStateFlow<List<com.example.trial.data.local.entities.CategoriaEntity>>(emptyList())
    val todasLasCategorias: StateFlow<List<com.example.trial.data.local.entities.CategoriaEntity>> = _todasLasCategorias.asStateFlow()

    // Cache para la categoría de Ingreso
    private var ingresoCategory: com.example.trial.data.local.entities.CategoriaEntity? = null

    init {
        viewModelScope.launch {
            // Verificar qué categorías hay en la BD
            val todasCategorias = categoriaRepository.getAllCategorias().first()
            println("🔍 CATEGORÍAS EN BD: ${todasCategorias.size}")
            todasCategorias.forEach { categoria ->
                println("   - ${categoria.idCategoria}: ${categoria.nombre}")
            }
        }
        loadCategorias()
    }

    private fun loadCategorias() {
        viewModelScope.launch {
            categoriaRepository.getAllCategorias().collect { todasLasCategorias ->
                // Guardar todas las categorías
                _todasLasCategorias.value = todasLasCategorias

                // Guardar la categoría de Ingreso para uso posterior
                ingresoCategory = todasLasCategorias.firstOrNull { it.nombre.lowercase() == "ingreso" }

                // Filtrar para el selector (excluir "Ingreso")
                val categoriasFiltradas = todasLasCategorias.filter { it.nombre.lowercase() != "ingreso" }
                _categoriasFiltradas.value = categoriasFiltradas
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

    fun onIncomeToggle(isIncome: Boolean) {
        _uiState.update { it.copy(isIncome = isIncome) }

        if (isIncome) {
            // Si es ingreso, asignar automáticamente la categoría "Ingreso"
            ingresoCategory?.let { categoria ->
                onCategoryChange(categoria.idCategoria, categoria.nombre)
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

        // Determinar el monto final (positivo para ingresos, negativo para gastos)
        val monto = if (currentState.isIncome) montoInput else -montoInput

        // Validar que se haya seleccionado una categoría
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
                    idCuenta = 1 // Asumiendo que la cuenta por defecto es 1
                )

                transaccionRepository.addTransaccion(transaccion)

                // Actualizar progreso de meta solo si es un gasto (monto negativo)
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
                    monto = -amount, // rápido siempre es gasto
                    idCategoria = categoryId,
                    descripcion = "Transacción rápida",
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

    fun getCategoryNameById(idCategoria: Int): String {
        // Buscar en todas las categorías
        return _todasLasCategorias.value
            .firstOrNull { it.idCategoria == idCategoria }?.nombre ?: "Desconocido"
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
            // Silenciar errores de metas para no interrumpir la transacción principal
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