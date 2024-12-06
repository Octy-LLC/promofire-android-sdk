package io.promofire.interactors

import io.promofire.data.mappers.toDto
import io.promofire.data.mappers.toModel
import io.promofire.data.network.api.codes.models.RedeemCodeRequestDto
import io.promofire.data.network.api.codesApi
import io.promofire.data.network.core.mapToPromofireResult
import io.promofire.models.Code
import io.promofire.models.CodeRedeems
import io.promofire.models.Codes
import io.promofire.models.params.GenerateCodeParams
import io.promofire.models.params.GenerateCodesParams
import io.promofire.utils.PromofireResult
import io.promofire.utils.toIsoString
import java.util.Date

internal class CodesInteractor {

    companion object {
        private const val PLATFORM_ANDROID = "ANDROID"
    }

    suspend fun getCurrentUserCodes(limit: Int, offset: Int): PromofireResult<Codes> {
        return codesApi.getMyCodes(limit, offset).mapToPromofireResult { it.toModel() }
    }

    suspend fun getCurrentUserRedeems(
        limit: Int,
        offset: Int,
        from: Date,
        to: Date,
    ): PromofireResult<CodeRedeems> = codesApi.getMyRedeems(limit, offset, from.toIsoString(), to.toIsoString())
        .mapToPromofireResult { it.toModel() }

    suspend fun generateCode(params: GenerateCodeParams): PromofireResult<Code> {
        return codesApi.createCode(params.toDto()).mapToPromofireResult { it.toModel() }
    }

    suspend fun generateCodes(params: GenerateCodesParams): PromofireResult<List<Code>> {
        return codesApi.createCodes(params.toDto()).mapToPromofireResult { codes -> codes.map { it.toModel() } }
    }

    suspend fun redeemCode(codeValue: String): PromofireResult<Unit> {
        val redeemCodeRequest = RedeemCodeRequestDto(codeValue = codeValue, platform = PLATFORM_ANDROID)
        return codesApi.redeemCode(redeemCodeRequest).mapToPromofireResult()
    }
}
