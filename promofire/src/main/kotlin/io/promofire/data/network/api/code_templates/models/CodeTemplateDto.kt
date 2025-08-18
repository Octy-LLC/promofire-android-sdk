package io.promofire.data.network.api.code_templates.models

import io.promofire.data.network.core.serializers.IsoDateSerializer
import io.promofire.models.CodeTemplate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.Date

@Serializable
internal data class CodeTemplateDto(
    val id: String,
    val name: String,
    val creatorId: String,
    val status: CodeTemplate.Status,
    @Serializable(IsoDateSerializer::class)
    val createdAt: Date,
    val ttl: Long,
    val amount: String,
    val hasMutablePayload: Boolean,
    val isUsableByCustomers: Boolean,
    val description: String?,
    val payload: JsonElement,
)
