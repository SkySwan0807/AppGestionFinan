package com.example.trial.observer

interface IObserver<T> {

    // La acción que realizará el observer cuando el subject actualice los datos
    fun update(value: T)
}
