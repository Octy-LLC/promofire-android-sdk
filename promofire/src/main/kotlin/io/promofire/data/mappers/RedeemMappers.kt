package io.promofire.data.mappers

import io.promofire.data.network.api.codes.models.RedeemDto
import io.promofire.data.network.api.codes.models.RedeemsDto
import io.promofire.models.Redeem
import io.promofire.models.Redeems

internal fun RedeemsDto.toModel(): Redeems = Redeems(
    redeems = redeems.map(RedeemDto::toModel),
    total = total,
)

internal fun RedeemDto.toModel(): Redeem = Redeem(
    id = id,
    redeemerId = redeemerId,
    code = code,
    platform = platform,
    country = country,
    templateId = templateId,
    redeemedAt = redeemedAt,
)
