package io.promofire.data.network.api.customers

import io.ktor.client.HttpClient
import io.promofire.data.network.api.common.models.AuthResponseDto
import io.promofire.data.network.api.customers.models.CreateCustomerRequestDto
import io.promofire.data.network.api.customers.models.CreatePresetRequestDto
import io.promofire.data.network.api.customers.models.CustomerDto
import io.promofire.data.network.api.customers.models.UpdateCustomerSelfDto
import io.promofire.data.network.core.NetworkResult
import io.promofire.data.network.core.safeGet
import io.promofire.data.network.core.safePatch
import io.promofire.data.network.core.safePost
import io.promofire.data.network.core.safePut

internal interface CustomersApi {

    suspend fun createCustomerPreset(
        request: CreatePresetRequestDto,
    ): NetworkResult<AuthResponseDto>

    suspend fun upsertCustomer(
        request: CreateCustomerRequestDto,
    ): NetworkResult<AuthResponseDto>

    suspend fun updateMyCustomer(
        request: UpdateCustomerSelfDto,
    ): NetworkResult<CustomerDto>

    suspend fun getMyCustomer(): NetworkResult<CustomerDto>
}

internal class CustomersApiImpl(
    private val httpClient: HttpClient,
) : CustomersApi {

    override suspend fun createCustomerPreset(
        request: CreatePresetRequestDto,
    ): NetworkResult<AuthResponseDto> = httpClient.safePost("customers/preset", request)

    override suspend fun upsertCustomer(
        request: CreateCustomerRequestDto,
    ): NetworkResult<AuthResponseDto> = httpClient.safePut("customers", request)

    override suspend fun updateMyCustomer(
        request: UpdateCustomerSelfDto,
    ): NetworkResult<CustomerDto> = httpClient.safePatch("customers/me", request)

    override suspend fun getMyCustomer(): NetworkResult<CustomerDto> {
        return httpClient.safeGet("customers/me")
    }
}
