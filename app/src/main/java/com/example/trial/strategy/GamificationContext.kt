package com.example.trial.strategy

/**
 * Contexto
 * Utiliza una estrategia para ejecutar la lógica de negocio.
 * Permite cambiar la estrategia en tiempo de ejecución.
 */
class GamificationContext(private var strategy: AchievementStrategy) {

    fun setStrategy(strategy: AchievementStrategy) {
        this.strategy = strategy
    }

    fun executeAnalysis(spending: Double, limit: Double): String {
        return strategy.analyze(spending, limit)
    }
    
    fun getCurrentBadge(): String {
        return strategy.getBadgeName()
    }
}
