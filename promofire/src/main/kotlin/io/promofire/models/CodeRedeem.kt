package io.promofire.models

import java.util.Date

public data class CodeRedeem(
    val id: String,
    val redeemerId: String,
    val code: String,
    val platform: String?,
    val country: String?,
    val templateId: String,
    val redeemedAt: Date,
)
