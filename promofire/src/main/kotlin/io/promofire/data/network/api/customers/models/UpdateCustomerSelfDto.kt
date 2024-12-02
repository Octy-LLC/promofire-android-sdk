package io.promofire.data.network.api.customers.models

import kotlinx.serialization.Serializable

@Serializable
internal data class UpdateCustomerSelfDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
)
