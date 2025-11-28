package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.GoalEntity
import com.example.trial.data.repository.GoalRepository
import com.example.trial.data.repository.IExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class GoalWithProgress(
    val goal: GoalEntity,
    val progress: Float,
    val remainingAmount: Double,
    val remainingDays: Int
)

data class GoalUiState(
    val goals: List<GoalWithProgress> = emptyList(),
    val category: String = "Transporte",
    val targetAmount: String = "",
    val months: String = "1",
    val description: String = "",
    val isLoading: Boolean = false,
    val showSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val expenseRepository: IExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            goalRepository.getActiveGoals()
                .map { goals ->
                    goals.map { goal ->
                        val (startMonth, endMonth) = getMonthRange()
                        val currentSpent = expenseRepository.getTotalByCategoryAndPeriod(
                            goal.category,
                            startMonth,
                            endMonth
                        )
                        
                        val progress = if (goal.targetAmount > 0) {
                            (currentSpent / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
                        } else 0f

                        val remainingDays = calculateRemainingDays(goal.endDate)

                        GoalWithProgress(
                            goal = goal.copy(currentAmount = currentSpent),
                            progress = progress,
                            remainingAmount = (goal.targetAmount - currentSpent).coerceAtLeast(0.0),
                            remainingDays = remainingDays
                        )
                    }
                }
                .collect { goalsWithProgress ->
                    _uiState.update { it.copy(goals = goalsWithProgress, isLoading = false) }
                }
        }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
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

    fun createGoal() {
        val currentState = _uiState.value
        val amount = currentState.targetAmount.toDoubleOrNull()
        val months = currentState.months.toIntOrNull()

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
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.MONTH, months)
                val endDate = calendar.timeInMillis

                val goal = GoalEntity(
                    category = currentState.category,
                    targetAmount = amount,
                    currentAmount = 0.0,
                    startDate = startDate,
                    endDate = endDate,
                    description = currentState.description.ifBlank { null }
                )

                goalRepository.insertGoal(goal)

                _uiState.update {
                    GoalUiState(
                        goals = it.goals,
                        showSuccess = true,
                        category = currentState.category
                    )
                }
                
                loadGoals()
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

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            try {
                goalRepository.deleteGoal(goal)
                loadGoals()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al eliminar: ${e.message}") }
            }
        }
    }

    private fun calculateRemainingDays(endDate: Long): Int {
        val now = System.currentTimeMillis()
        val diff = endDate - now
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    fun clearSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    val categories = listOf(
        "Alimentación",
        "Transporte",
        "Servicios",
        "Ocio",
        "Otros"
    )
}
