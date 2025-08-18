package io.promofire.data.network.api.customers.models

import io.promofire.models.Platform
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateCustomerRequestDto(
    val platform: Platform,
    val device: String,
    val os: String,
    val appBuild: String,
    val appVersion: String,
    val sdkVersion: String,
    val customerUserId: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
)
