package io.promofire.data.network.api.code_templates.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CodeTemplatesDto(
    val templates: List<CodeTemplateDto>,
    val total: Int,
)
