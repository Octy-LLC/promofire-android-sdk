package io.promofire.data.network.api.customers.models

import io.promofire.data.network.core.serializers.IsoDateSerializer
import io.promofire.models.Platform
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
internal data class CustomerDto(
    val id: String,
    val tenantAssignedId: String?,
    val country: String?,
    val platform: Platform,
    val device: String?,
    val os: String?,
    val appBuild: String?,
    val appVersion: String?,
    val sdkVersion: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
    @Serializable(IsoDateSerializer::class)
    val createdAt: Date,
    @Serializable(IsoDateSerializer::class)
    val lastSession: Date,
    val description: String?,
)
