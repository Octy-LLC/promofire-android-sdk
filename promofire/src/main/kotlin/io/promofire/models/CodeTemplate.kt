package io.promofire.models

import java.util.Date

public data class CodeTemplate(
    val id: String,
    val name: String,
    val creatorId: String,
    val status: Status,
    val createdAt: Date,
    /** Время жизни кода в миллисекундах. */
    val ttl: Long,
    /** Максимум погашений на код: либо `"Infinity"`, либо целое число строкой. */
    val amount: String,
    val hasMutablePayload: Boolean,
    /** Может ли клиент выпускать себе коды из этого шаблона. */
    val isUsableByCustomers: Boolean,
    val description: String?,
    val payload: Map<String, String>,
) {

    public enum class Status {
        ACTIVE, DEACTIVATED, ARCHIVED
    }
}
