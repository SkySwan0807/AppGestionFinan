package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.dao.CategorySum
import com.example.trial.data.local.entities.ExpenseEntity
import com.example.trial.data.repository.IExpenseRepository
import com.example.trial.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val totalBalance: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val categoryBreakdown: List<CategorySum> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList(),
    val comparisonWithLastMonth: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: IExpenseRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val (startOfMonth, endOfMonth) = getCurrentMonthRange()
            val (startLastMonth, endLastMonth) = getLastMonthRange()

            combine(
                expenseRepository.getExpensesBetween(startOfMonth, endOfMonth),
                expenseRepository.getSumByCategory(startOfMonth, endOfMonth),
                expenseRepository.getAllExpenses()
            ) { currentMonth, breakdown, all ->
                val monthlyTotal = currentMonth.sumOf { it.amount }
                val lastMonthTotal = calculateLastMonthTotal(startLastMonth, endLastMonth)
                
                HomeUiState(
                    totalBalance = 1000.0, // Esto debería venir de configuración
                    monthlyExpenses = monthlyTotal,
                    categoryBreakdown = breakdown,
                    recentExpenses = all.take(10),
                    comparisonWithLastMonth = lastMonthTotal - monthlyTotal,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun calculateLastMonthTotal(start: Long, end: Long): Double {
        return expenseRepository.getTotalBetween(start, end)
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endOfMonth = calendar.timeInMillis

        return Pair(startOfMonth, endOfMonth)
    }

    private fun getLastMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startLastMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endLastMonth = calendar.timeInMillis

        return Pair(startLastMonth, endLastMonth)
    }

    fun refresh() {
        loadData()
    }
}
