package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.entities.ExpenseEntity
import com.example.trial.data.repository.IExpenseRepository
import com.example.trial.data.repository.GoalRepository
import com.example.trial.data.repository.BadgeRepository
import com.example.trial.data.local.entities.BadgeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ExpenseUiState(
    val amount: String = "",
    val category: String = "Alimentación",
    val note: String = "",
    val isLoading: Boolean = false,
    val showSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: IExpenseRepository,
    private val goalRepository: GoalRepository,
    private val badgeRepository: BadgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val categories = listOf(
        "Alimentación",
        "Transporte",
        "Servicios",
        "Ocio",
        "Otros"
    )

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun addExpense() {
        val currentState = _uiState.value
        val amount = currentState.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Monto inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val expense = ExpenseEntity(
                    amount = amount,
                    category = currentState.category,
                    note = currentState.note.ifBlank { null },
                    date = System.currentTimeMillis()
                )

                expenseRepository.insertExpense(expense)
                
                // Actualizar meta si existe
                updateGoalProgress(currentState.category, amount)
                
                // Verificar si se gana insignia
                checkForBadges(currentState.category, amount)

                _uiState.update {
                    ExpenseUiState(
                        showSuccess = true,
                        category = currentState.category
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

    fun addQuickExpense(amount: Double, category: String) {
        viewModelScope.launch {
            try {
                val expense = ExpenseEntity(
                    amount = amount,
                    category = category,
                    note = "Gasto rápido",
                    date = System.currentTimeMillis()
                )

                expenseRepository.insertExpense(expense)
                updateGoalProgress(category, amount)
                
                _uiState.update { it.copy(showSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    private suspend fun updateGoalProgress(category: String, amount: Double) {
        val goal = goalRepository.getActiveGoalByCategory(category)
        if (goal != null) {
            val newAmount = goal.currentAmount + amount
            goalRepository.updateCurrentAmount(goal.id, newAmount)

            if (newAmount >= goal.targetAmount) {
                goalRepository.markAsCompleted(goal.id)
            }
        }
    }

    private suspend fun checkForBadges(category: String, amount: Double) {
        val (startOfMonth, endOfMonth) = getCurrentMonthRange()
        val totalThisMonth = expenseRepository.getTotalByCategoryAndPeriod(category, startOfMonth, endOfMonth)
        
        // Insignia por gastar menos de 100 en una categoría
        if (totalThisMonth < 100) {
            val badge = BadgeEntity(
                name = "Ahorrador de $category",
                description = "Gastaste menos de 100 BoB en $category este mes",
                type = "saver",
                earnedDate = System.currentTimeMillis(),
                iconResName = "ic_badge_saver"
            )
            badgeRepository.insertBadge(badge)
        }
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
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

    fun getCategories() = categories
}
