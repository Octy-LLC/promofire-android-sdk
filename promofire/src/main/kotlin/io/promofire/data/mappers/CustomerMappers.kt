package io.promofire.data.mappers

import io.promofire.data.network.api.customers.models.CustomerDto
import io.promofire.data.network.api.customers.models.UpdateCustomerSelfDto
import io.promofire.models.Customer
import io.promofire.models.params.UpdateCustomerParams

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

internal fun UpdateCustomerParams.toDto(): UpdateCustomerSelfDto = UpdateCustomerSelfDto(
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
)
