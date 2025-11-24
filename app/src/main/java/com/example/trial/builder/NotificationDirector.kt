package com.example.trial.builder

import com.example.trial.R

/**
 * Director
 * Define el orden en el que se ejecutan los pasos de construcción
 * para crear configuraciones predefinidas de notificaciones.
 */
class NotificationDirector(private val builder: INotificationBuilder) {

    fun constructAchievementNotification(message: String): NotificationProduct {
        return builder
            .buildTitle("¡Nuevo Logro Desbloqueado!")
            .buildMessage(message)
            .buildIcon(R.drawable.ic_launcher_foreground) // Icono de ejemplo
            .buildPriority(1) // Alta prioridad
            .buildAction("Ver Logros")
            .build()
    }

    fun constructWarningNotification(message: String): NotificationProduct {
        return builder
            .buildTitle("Alerta de Presupuesto")
            .buildMessage(message)
            .buildIcon(R.drawable.ic_notifications_black_24dp) // Icono de alerta
            .buildPriority(2) // Máxima prioridad
            .buildAction("Revisar Gastos")
            .build()
    }
}
