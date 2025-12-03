package com.example.trial.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trial.data.local.dao.CategorySum
import com.example.trial.data.local.entities.TransaccionEntity
import com.example.trial.data.repository.TransaccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val totalBalance: Double = 0.0,
    val monthlyexpenses: Double = 0.0,
    val totalSpending: Double = 0.0,
    val categoryBreakdown: List<CategorySum> = emptyList(),
    val recentTransfers: List<TransaccionEntity> = emptyList(),
    val comparisonWithLastMonth: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transaccionRepository: TransaccionRepository,
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

            // Obtenemos flujos de datos
            val monthlyTransactionsFlow = transaccionRepository.getTransfersBetween(startOfMonth, endOfMonth)
            val categoryBreakdownFlow = transaccionRepository.getSumByCategoryFecha(startOfMonth, endOfMonth)
            val allTransactionsFlow = transaccionRepository.getAllTransacciones()
            val totalSpendingFlow = transaccionRepository.totalSpending()

            combine(
                monthlyTransactionsFlow,
                categoryBreakdownFlow,
                allTransactionsFlow,
                totalSpendingFlow
            ) { monthlyTxs, breakdown, allTxs, totalSpending ->

                val monthlyExpenses = monthlyTxs
                    .filter { it.monto < 0 }  // solo gastos
                    .sumOf { it.monto }

                val totalBalance = totalSpending ?: 0.0
                val lastMonthTotal = calculateLastMonthTotal(startLastMonth, endLastMonth)

                HomeUiState(
                    totalBalance = totalBalance,
                    monthlyexpenses = monthlyExpenses,
                    totalSpending = totalBalance,
                    categoryBreakdown = breakdown,
                    recentTransfers = allTxs.take(10),
                    comparisonWithLastMonth = lastMonthTotal - monthlyExpenses,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun calculateLastMonthTotal(start: Long, end: Long): Double {
        return transaccionRepository.getTotalBetween(start, end)
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
        calendar.set(Calendar.SECOND, 59)
        val endOfMonth = calendar.timeInMillis

        return Pair(startOfMonth, endOfMonth)
    }

    private fun getLastMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startLastMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endLastMonth = calendar.timeInMillis

        return Pair(startLastMonth, endLastMonth)
    }

    fun refresh() {
        loadData()
    }
}
