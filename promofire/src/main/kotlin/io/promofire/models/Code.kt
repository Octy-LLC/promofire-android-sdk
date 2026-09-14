package io.promofire.models

import java.util.Date

public data class Code(
    val value: String,
    val status: Status,
    val templateId: String,
    val createdAt: Date,
    val updatedAt: Date,
    /** Unix-время истечения в миллисекундах. */
    val expiresAt: Long,
    /** Клиент или сотрудник, которому принадлежит код. */
    val ownerId: String,
    val payload: Map<String, String>,
    /** Остаток погашений: либо `"Infinity"`, либо целое число строкой. */
    val amount: String,
) {

    public enum class Status {
        ACTIVE, FULLY_REDEEMED, DEACTIVATED, EXPIRED
    }
}
