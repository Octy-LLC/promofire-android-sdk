package io.promofire.models

import java.util.Date

public data class Customer(
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
    val createdAt: Date,
    val lastSession: Date,
    val description: String?,
)
