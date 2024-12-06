package io.promofire.interactors

import io.promofire.data.mappers.toModel
import io.promofire.data.network.api.codesApi
import io.promofire.data.network.core.mapToPromofireResult
import io.promofire.models.Codes
import io.promofire.utils.PromofireResult

internal class CodesInteractor {

    suspend fun getCurrentUserCodes(limit: Int, offset: Int): PromofireResult<Codes> {
        return codesApi.getMyCodes(limit, offset).mapToPromofireResult { it.toModel() }
    }
}
