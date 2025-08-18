package io.promofire.models

import io.promofire.utils.INFINITY
import io.promofire.utils.timeInSeconds
import java.util.Date

public data class Code(
    val value: String,
    val status: Status,
    val templateId: String,
    val createdAt: Date,
    val updatedAt: Date,
    val expiresAt: Long,
    val ownerId: String,
    val payload: String,
    val amount: String,
) {

    public enum class Status {
        ACTIVE, FULLY_REDEEMED, DEACTIVATED
    }
}

public val Code.isValid: Boolean
    get() = isValidByAmount && !isExpired && status == Code.Status.ACTIVE

private val Code.isInfiniteCode: Boolean
    get() = amount.equals(INFINITY, ignoreCase = true)

private val Code.isExpired: Boolean
    get() {
        val currentTimeSeconds = Date().timeInSeconds
        return currentTimeSeconds >= expiresAt
    }

private val Code.amountValue: Int?
    get() = if (isInfiniteCode) null else amount.toIntOrNull()

private val Code.isValidByAmount: Boolean
    get() = isInfiniteCode || (amountValue ?: 0) > 0
