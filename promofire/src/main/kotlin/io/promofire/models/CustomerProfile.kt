package io.promofire.models

/** Необязательные поля профиля, которыми можно заполнить клиента при `identify`. */
public data class CustomerProfile(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
