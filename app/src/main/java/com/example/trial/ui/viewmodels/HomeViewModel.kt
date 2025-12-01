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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

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

            // Flows que expone tu TransaccionRepository
            val monthlyTransactionsFlow =
                transaccionRepository.getTransfersBetweenActivas(startOfMonth, endOfMonth)
            val categoryBreakdownFlow =
                transaccionRepository.getSumByCategoryActivas(startOfMonth, endOfMonth)
            val allTransactionsFlow =
                transaccionRepository.getAllTransaccionesActivas()
            val totalSpendingFlow =
                transaccionRepository.totalSpendingActivas()



            // Total del mes pasado (suspend)
            val lastMonthTotal = try {
                transaccionRepository.getTotalBetween(startLastMonth, endLastMonth)
            } catch (e: Exception) {
                0.0
            }

            combine<
                    List<TransaccionEntity>,
                    List<CategorySum>,
                    List<TransaccionEntity>,
                    Double?,
                    HomeUiState
                    >(
                monthlyTransactionsFlow,
                categoryBreakdownFlow,
                allTransactionsFlow,
                totalSpendingFlow
            ) { monthlyTxs, breakdown, allTxs, totalSpending ->

                val monthlyExpenses = monthlyTxs
                    .filter { it.monto < 0.0 }
                    .sumOf { it.monto }

                val totalBalance = totalSpending ?: 0.0

                HomeUiState(
                    totalBalance = totalBalance,
                    monthlyexpenses = monthlyExpenses,
                    totalSpending = totalBalance,
                    categoryBreakdown = breakdown,
                    recentTransfers = allTxs.take(10),
                    comparisonWithLastMonth = lastMonthTotal - monthlyExpenses,
                    isLoading = false
                )
            }
                .catch {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
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
