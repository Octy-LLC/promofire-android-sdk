package io.promofire.data.mappers

import io.promofire.data.network.api.codes.models.CodeDto
import io.promofire.data.network.api.codes.models.CodesDto
import io.promofire.data.network.api.codes.models.CreateCodeDto
import io.promofire.models.Code
import io.promofire.models.Codes
import io.promofire.models.params.CreateCodeParams

internal fun CodesDto.toModel(): Codes = Codes(
    codes = codes.map(CodeDto::toModel),
    total = total,
)

internal fun CodeDto.toModel(): Code = Code(
    value = value,
    status = status,
    templateId = templateId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    expiresAt = expiresAt,
    ownerId = ownerId,
    payload = payload.toString(),
    amount = amount,
)

internal fun CreateCodeParams.toDto(): CreateCodeDto = CreateCodeDto(
    value = value,
    templateId = templateId,
    payload = payload.toJsonElement(),
)
