package io.promofire.data.network.api.auth

import io.ktor.client.HttpClient
import io.promofire.data.network.api.auth.requests.SdkCustomerAuthRequestDto
import io.promofire.data.network.api.common.models.AuthResponseDto
import io.promofire.data.network.core.NetworkResult
import io.promofire.data.network.core.safePost

internal interface AuthApi {

    suspend fun signInWithSdk(
        request: SdkCustomerAuthRequestDto,
    ): NetworkResult<AuthResponseDto>
}

internal class AuthApiImpl(
    private val httpClient: HttpClient,
) : AuthApi {

    override suspend fun signInWithSdk(
        request: SdkCustomerAuthRequestDto,
    ): NetworkResult<AuthResponseDto> = httpClient.safePost("auth/sdk/customer", request)
}
