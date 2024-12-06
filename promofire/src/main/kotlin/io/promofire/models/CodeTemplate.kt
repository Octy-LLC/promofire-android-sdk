package io.promofire.models

import java.util.Date

public data class CodeTemplate(
    val id: String,
    val name: String,
    val creatorId: String,
    val status: Status,
    val createdAt: Date,
    val ttl: Long,
    val amount: String,
    val hasMutablePayload: Boolean,
    val isUsableByCustomers: Boolean,
    val description: String?,
    val payload: String,
) {

    public enum class Status {
        ACTIVE, DEACTIVATED, ARCHIVED
    }
}
