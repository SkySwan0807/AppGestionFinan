package com.example.trial.builder

/**
 * Producto
 * Representa la notificación compleja que estamos construyendo.
 */
data class NotificationProduct(
    var title: String = "",
    var message: String = "",
    var iconResId: Int = 0,
    var priority: Int = 0,
    var actionLabel: String? = null
)
