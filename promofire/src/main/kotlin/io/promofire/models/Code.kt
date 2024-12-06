package io.promofire.models

import java.util.Date

public data class Code(
    val value: String,
    val status: Status,
    val templateId: String,
    val createdAt: Date,
    val updatedAt: Date,
    val expiresAt: Int,
    val ownerId: String,
    val payload: String,
    val amount: String,
) {

    public enum class Status {
        ACTIVE, FULLY_REDEEMED, DEACTIVATED
    }
}
