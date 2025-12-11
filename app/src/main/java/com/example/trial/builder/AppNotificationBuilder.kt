package com.example.trial.builder

/**
 * Builder Concreto
 * Implementa los pasos para construir el objeto NotificationProduct.
 */
class AppNotificationBuilder : INotificationBuilder {
    private val product = NotificationProduct()

    override fun buildTitle(title: String): INotificationBuilder {
        product.title = title
        return this
    }

    override fun buildMessage(message: String): INotificationBuilder {
        product.message = message
        return this
    }

    override fun buildIcon(iconResId: Int): INotificationBuilder {
        product.iconResId = iconResId
        return this
    }

    override fun buildPriority(priority: Int): INotificationBuilder {
        product.priority = priority
        return this
    }

    override fun buildAction(label: String): INotificationBuilder {
        product.actionLabel = label
        return this
    }

    override fun build(): NotificationProduct {
        return product
    }
}

