package com.example.trial.observer

class LiveSubject<T> : ISubject<T> {

    private val observers = mutableListOf<IObserver<T>>()

    private var _value: T? = null
    val value: T?
        get() = _value

    fun setValue(newValue: T) {
        _value = newValue
        notifyObservers()
    }

    override fun attach(observer: IObserver<T>) {
        observers.add(observer)
    }

    override fun detach(observer: IObserver<T>) {
        observers.remove(observer)
    }

    override fun notifyObservers() {
        _value?.let { value ->
            observers.forEach { it.update(value) }
        }
    }
}
