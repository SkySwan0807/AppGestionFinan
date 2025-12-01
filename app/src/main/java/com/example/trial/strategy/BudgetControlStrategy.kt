package com.example.trial.strategy

/**
 * Estrategia Concreta: Control de Presupuesto
 * Se enfoca en alertar si se excede el presupuesto.
 */
class BudgetControlStrategy : AchievementStrategy {
    override fun analyze(currentSpending: Double, limit: Double): String {
        return if (currentSpending > limit) {
            val excess = currentSpending - limit
            "Alerta: Has excedido tu presupuesto por $excess Bs."
        } else {
            "Mantienes un buen control de tus finanzas."
        }
    }

    override fun getBadgeName(): String {
        return "Insignia de Gestor Responsable"
    }
}
