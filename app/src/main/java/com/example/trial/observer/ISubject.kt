package com.example.trial.observer

interface ISubject<T> {

    // Registrar un observador
    fun attach(observer: IObserver<T>)

    // Remover un observador
    fun detach(observer: IObserver<T>)

    // Notificar a todos los observadores
    fun notifyObservers()
}
