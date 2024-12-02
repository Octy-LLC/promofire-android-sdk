package io.promofire.data.network.api.code_templates.models

import io.promofire.data.network.core.serializers.IsoDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.Date

@Serializable
internal data class CodeTemplateDto(
    val id: String,
    val name: String,
    val creatorId: String,
    val status: Status,
    @Serializable(IsoDateSerializer::class)
    val createdAt: Date,
    val ttl: Int,
    val amount: String,
    val hasMutablePayloads: Boolean,
    val isUsableByCustomers: Boolean,
    val description: String?,
    val payload: JsonElement,
) {

    enum class Status {
        ACTIVE, DEACTIVATED, ARCHIVED
    }
}
