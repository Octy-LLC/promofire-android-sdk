package io.promofire.data.mappers

import io.promofire.data.network.api.customers.models.CustomerDto
import io.promofire.models.Customer

internal fun CustomerDto.toModel(): Customer = Customer(
    id = id,
    tenantAssignedId = tenantAssignedId,
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
