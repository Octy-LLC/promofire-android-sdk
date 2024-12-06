package io.promofire.interactors

import io.promofire.data.mappers.toModel
import io.promofire.data.network.api.codeTemplatesApi
import io.promofire.data.network.core.mapToPromofireResult
import io.promofire.models.CodeTemplates
import io.promofire.utils.PromofireResult

internal class CodeTemplatesInteractor {

    internal suspend fun getCampaigns(limit: Int, offset: Int): PromofireResult<CodeTemplates> {
        return codeTemplatesApi.getCodeTemplates(limit, offset).mapToPromofireResult { it.toModel() }
    }
}
