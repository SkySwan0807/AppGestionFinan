package com.example.trial.strategy

/**
 * Interfaz Strategy
 * Define el contrato para las diferentes estrategias de gamificación o análisis de gastos.
 */
interface AchievementStrategy {
    fun analyze(currentSpending: Double, limit: Double): String
    fun getBadgeName(): String
}
