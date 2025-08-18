package io.promofire.data.mappers

import io.promofire.data.network.api.codes.models.CodeDto
import io.promofire.data.network.api.codes.models.CodesDto
import io.promofire.data.network.api.codes.models.CreateCodeDto
import io.promofire.data.network.api.codes.models.CreateCodesDto
import io.promofire.data.network.api.codes.models.UpdateCodeDto
import io.promofire.models.Code
import io.promofire.models.Codes
import io.promofire.models.params.GenerateCodeParams
import io.promofire.models.params.GenerateCodesParams
import io.promofire.models.params.UpdateCodeParams

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

internal fun GenerateCodesParams.toDto(): CreateCodesDto = CreateCodesDto(
    templateId = templateId,
    payload = payload.toJsonElement(),
    count = count,
)

internal fun GenerateCodeParams.toDto(): CreateCodeDto = CreateCodeDto(
    value = value,
    templateId = templateId,
    payload = payload.toJsonElement(),
)

internal fun UpdateCodeParams.toDto(): UpdateCodeDto = UpdateCodeDto(
    active = isActive,
    payload = payload.toJsonElement(),
)
