package io.promofire.data.mappers

import io.promofire.data.network.api.codes.models.CodeRedeemDto
import io.promofire.data.network.api.codes.models.CodeRedeemsDto
import io.promofire.models.CodeRedeem
import io.promofire.models.CodeRedeems

internal fun CodeRedeemsDto.toModel(): CodeRedeems = CodeRedeems(
    redeems = redeems.map(CodeRedeemDto::toModel),
    total = total,
)

internal fun CodeRedeemDto.toModel(): CodeRedeem = CodeRedeem(
    id = id,
    redeemerId = redeemerId,
    code = code,
    platform = platform,
    country = country,
    templateId = templateId,
    redeemedAt = redeemedAt,
)
