package io.promofire.internal

import io.promofire.models.Code
import io.promofire.models.CodeRedeem
import io.promofire.models.CodeTemplate
import io.promofire.models.Customer
import io.promofire.models.PaginatedList

internal fun CodeDto.toModel(): Code = Code(
    value = value,
    status = status,
    templateId = templateId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    expiresAt = expiresAt,
    ownerId = ownerId,
    payload = payload,
    amount = amount,
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
    payload = payload,
)

internal fun CustomerDto.toModel(): Customer = Customer(
    id = id,
    customerUserId = customerUserId,
    country = country,
    platform = platform,
    device = device,
    os = os,
    appBuild = appBuild,
    appVersion = appVersion,
    sdkVersion = sdkVersion,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    createdAt = createdAt,
    lastSession = lastSession,
    description = description,
)

// API не использует общего имени для массива: `/codes/me` отдаёт `codes`,
// `/codes/redeems/me` — `redeems`, `/code-templates` — `templates`.
// Здесь все три приводятся к единому PaginatedList.

internal fun CodesDto.toModel(): PaginatedList<Code> = PaginatedList(
    items = codes.map(CodeDto::toModel),
    total = total,
)

internal fun CodeRedeemsDto.toModel(): PaginatedList<CodeRedeem> = PaginatedList(
    items = redeems.map(CodeRedeemDto::toModel),
    total = total,
)

internal fun CodeTemplatesDto.toModel(): PaginatedList<CodeTemplate> = PaginatedList(
    items = templates.map(CodeTemplateDto::toModel),
    total = total,
)
