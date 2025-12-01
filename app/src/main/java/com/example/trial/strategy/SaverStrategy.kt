package com.example.trial.strategy

/**
 * Estrategia Concreta: Ahorrador
 * Se enfoca en premiar al usuario si gasta menos de su límite.
 */
class SaverStrategy : AchievementStrategy {
    override fun analyze(currentSpending: Double, limit: Double): String {
        return if (currentSpending < limit) {
            val saved = limit - currentSpending
            "¡Excelente! Has ahorrado $saved Bs este periodo."
        } else {
            "Estás cerca del límite, intenta reducir gastos hormiga."
        }
    }

    override fun getBadgeName(): String {
        return "Insignia de Ahorro Maestro"
    }
}
