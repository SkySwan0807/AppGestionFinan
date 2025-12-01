package com.example.trial.builder

/**
 * Interfaz Builder
 * Define los pasos necesarios para construir una notificación.
 */
interface INotificationBuilder {
    fun buildTitle(title: String): INotificationBuilder
    fun buildMessage(message: String): INotificationBuilder
    fun buildIcon(iconResId: Int): INotificationBuilder
    fun buildPriority(priority: Int): INotificationBuilder
    fun buildAction(label: String): INotificationBuilder
    fun build(): NotificationProduct
}
