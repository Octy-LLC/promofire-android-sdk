package io.promofire.internal

import io.promofire.models.Code
import io.promofire.models.CodeTemplate
import io.promofire.models.Platform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
internal data class AuthRequestDto(
    val secret: String,
    val platform: Platform,
    val device: String,
    val os: String,
    val appBuild: String,
    val appVersion: String,
    val sdkVersion: String,
    val customerUserId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
)

@Serializable
internal data class AuthResponseDto(val accessToken: String)

@Serializable
internal data class CodeDto(
    val value: String,
    val status: Code.Status,
    val templateId: String,
    @Serializable(IsoDateSerializer::class) val createdAt: Date,
    @Serializable(IsoDateSerializer::class) val updatedAt: Date,
    val expiresAt: Long,
    val ownerId: String,
    val payload: Map<String, String> = emptyMap(),
    val amount: String,
)

@Serializable
internal data class CodesDto(
    val codes: List<CodeDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
internal data class CodeRedeemDto(
    val id: String,
    val redeemerId: String,
    val code: String,
    val platform: Platform,
    val country: String,
    val templateId: String,
    @Serializable(IsoDateSerializer::class) val redeemedAt: Date,
)

@Serializable
internal data class CodeRedeemsDto(
    val redeems: List<CodeRedeemDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
internal data class CodeTemplateDto(
    val id: String,
    val name: String,
    val creatorId: String,
    val status: CodeTemplate.Status,
    @Serializable(IsoDateSerializer::class) val createdAt: Date,
    val ttl: Long,
    val amount: String,
    val hasMutablePayload: Boolean,
    val isUsableByCustomers: Boolean,
    val description: String? = null,
    val payload: Map<String, String> = emptyMap(),
)

@Serializable
internal data class CodeTemplatesDto(
    @SerialName("templates") val templates: List<CodeTemplateDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
internal data class CustomerDto(
    val id: String,
    val customerUserId: String? = null,
    val country: String? = null,
    val platform: Platform,
    val device: String? = null,
    val os: String? = null,
    val appBuild: String? = null,
    val appVersion: String? = null,
    val sdkVersion: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @Serializable(IsoDateSerializer::class) val createdAt: Date,
    @Serializable(IsoDateSerializer::class) val lastSession: Date,
    val description: String? = null,
)

@Serializable
internal data class CreateCodeRequestDto(
    val value: String,
    val templateId: String,
    val payload: Map<String, String>,
)

@Serializable
internal data class CreateCodesRequestDto(
    val templateId: String,
    val payload: Map<String, String>,
    val count: Int,
)

@Serializable
internal data class UpdateCodeRequestDto(
    val payload: Map<String, String>? = null,
    val active: Boolean? = null,
)

@Serializable
internal data class UpdateCustomerRequestDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
)

@Serializable
internal data class RedeemRequestDto(
    val codeValue: String,
    val platform: Platform,
)
