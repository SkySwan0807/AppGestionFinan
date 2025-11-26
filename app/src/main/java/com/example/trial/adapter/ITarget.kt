package com.example.trial.adapter

interface ITarget<T> {
    // Devuelve cuántos elementos hay
    fun getItemCount(): Int

    // Devuelve un elemento en la posición solicitada
    fun getItem(position: Int): T
}