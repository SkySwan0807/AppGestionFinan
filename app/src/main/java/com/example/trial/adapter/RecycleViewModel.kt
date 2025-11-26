package com.example.trial.adapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.trial.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecycleViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allExpenses = repository.allExpenses.asLiveData()
    val totalSpending = repository.totalSpending.asLiveData()
}
