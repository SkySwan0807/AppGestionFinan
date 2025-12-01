package com.example.trial.observer

class LiveObserver<T>(
    private val onChange: (T) -> Unit
) : IObserver<T> {

    override fun update(value: T) {
        onChange(value)
    }
}
