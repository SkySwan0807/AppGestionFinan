package com.example.trial.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.plus

open class BaseViewModel : ViewModel() {
    private val handler = CoroutineExceptionHandler { _, throwable ->
        // Log or handle global exceptions
        throwable.printStackTrace()
    }
    protected val safeScope = viewModelScope + handler
}