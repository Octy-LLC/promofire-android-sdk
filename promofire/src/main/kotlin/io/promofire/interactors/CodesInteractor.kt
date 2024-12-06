package io.promofire.interactors

import io.promofire.data.mappers.toModel
import io.promofire.data.network.api.codesApi
import io.promofire.data.network.core.mapToPromofireResult
import io.promofire.models.CodeRedeems
import io.promofire.models.Codes
import io.promofire.utils.PromofireResult
import io.promofire.utils.toIsoString
import java.util.Date

internal class CodesInteractor {

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
}
