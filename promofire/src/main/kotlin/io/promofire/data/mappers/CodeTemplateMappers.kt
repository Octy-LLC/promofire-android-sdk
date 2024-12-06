package io.promofire.data.mappers

import io.promofire.data.network.api.code_templates.models.CodeTemplateDto
import io.promofire.data.network.api.code_templates.models.CodeTemplatesDto
import io.promofire.models.CodeTemplate
import io.promofire.models.CodeTemplates

internal fun CodeTemplatesDto.toModel(): CodeTemplates = CodeTemplates(
    templates = templates.map(CodeTemplateDto::toModel),
    total = total,
)

internal fun CodeTemplateDto.toModel(): CodeTemplate = CodeTemplate(
    id = id,
    name = name,
    creatorId = creatorId,
    status = status,
    createdAt = createdAt,
    ttl = ttl,
    amount = amount,
    hasMutablePayload = hasMutablePayload,
    isUsableByCustomers = isUsableByCustomers,
    description = description,
    payload = payload.toString(),
)
