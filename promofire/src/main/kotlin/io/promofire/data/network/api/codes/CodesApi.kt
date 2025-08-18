package io.promofire.data.network.api.codes

import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.promofire.data.network.api.codes.models.CodeDto
import io.promofire.data.network.api.codes.models.CodeRedeemsDto
import io.promofire.data.network.api.codes.models.CodesDto
import io.promofire.data.network.api.codes.models.CreateCodeDto
import io.promofire.data.network.api.codes.models.CreateCodesDto
import io.promofire.data.network.api.codes.models.RedeemCodeRequestDto
import io.promofire.data.network.api.codes.models.UpdateCodeDto
import io.promofire.data.network.core.EmptyNetworkResult
import io.promofire.data.network.core.NetworkResult
import io.promofire.data.network.core.safeGet
import io.promofire.data.network.core.safePatch
import io.promofire.data.network.core.safePost

internal interface CodesApi {

    suspend fun createCode(request: CreateCodeDto): NetworkResult<CodeDto>

    suspend fun createCodes(request: CreateCodesDto): NetworkResult<List<CodeDto>>

    suspend fun updateCode(codeValue: String, request: UpdateCodeDto): NetworkResult<CodeDto>

    suspend fun getMyCodes(limit: Int, offset: Int = 0): NetworkResult<CodesDto>

    suspend fun getCodeByValue(codeValue: String): NetworkResult<CodeDto>

    suspend fun redeemCode(request: RedeemCodeRequestDto): EmptyNetworkResult

    suspend fun getRedeems(
        limit: Int,
        offset: Int,
        from: String,
        to: String,
        codeValue: String? = null,
        redeemerId: String? = null,
    ): NetworkResult<CodeRedeemsDto>

    suspend fun getMyRedeems(
        limit: Int,
        offset: Int,
        from: String,
        to: String,
        codeValue: String?,
    ): NetworkResult<CodeRedeemsDto>
}

internal class CodesApiImpl(
    private val httpClient: HttpClient,
) : CodesApi {

    override suspend fun createCode(
        request: CreateCodeDto,
    ): NetworkResult<CodeDto> = httpClient.safePost("codes", request)

    override suspend fun createCodes(
        request: CreateCodesDto,
    ): NetworkResult<List<CodeDto>> = httpClient.safePost("codes/batch", request)

    override suspend fun updateCode(
        codeValue: String,
        request: UpdateCodeDto,
    ): NetworkResult<CodeDto> = httpClient.safePatch("codes/$codeValue", request)

    override suspend fun getMyCodes(
        limit: Int,
        offset: Int,
    ): NetworkResult<CodesDto> = httpClient.safeGet("codes/me?limit=$limit&offset=$offset")

    override suspend fun getCodeByValue(
        codeValue: String,
    ): NetworkResult<CodeDto> = httpClient.safeGet("codes/$codeValue")

    override suspend fun redeemCode(
        request: RedeemCodeRequestDto,
    ): EmptyNetworkResult = httpClient.safePost("codes/redeem", request)

    override suspend fun getRedeems(
        limit: Int,
        offset: Int,
        from: String,
        to: String,
        codeValue: String?,
        redeemerId: String?,
    ): NetworkResult<CodeRedeemsDto> = httpClient.safeGet("codes/redeems") {
        parameter("limit", limit)
        parameter("offset", offset)
        parameter("from", from)
        parameter("to", to)
        parameter("codeValue", codeValue)
        parameter("redeemerId", redeemerId)
    }

    override suspend fun getMyRedeems(
        limit: Int,
        offset: Int,
        from: String,
        to: String,
        codeValue: String?,
    ): NetworkResult<CodeRedeemsDto> = httpClient.safeGet("codes/redeems/me") {
        parameter("limit", limit)
        parameter("offset", offset)
        parameter("from", from)
        parameter("to", to)
        parameter("codeValue", codeValue)
    }
}
