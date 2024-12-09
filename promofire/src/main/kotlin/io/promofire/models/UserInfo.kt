package io.promofire.models

public data class UserInfo(
    public val customerUserId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
