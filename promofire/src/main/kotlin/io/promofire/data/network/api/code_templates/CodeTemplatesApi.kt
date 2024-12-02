package io.promofire.data.network.api.code_templates

import io.ktor.client.HttpClient
import io.promofire.data.network.api.code_templates.models.CodeTemplateDto
import io.promofire.data.network.api.code_templates.models.CodeTemplatesDto
import io.promofire.data.network.core.NetworkResult
import io.promofire.data.network.core.safeGet

internal interface CodeTemplatesApi {

    suspend fun getCodeTemplates(limit: Int, offset: Int = 0): NetworkResult<CodeTemplatesDto>

    suspend fun getCodeTemplate(id: String): NetworkResult<CodeTemplateDto>
}

internal class CodeTemplatesApiImpl(
    private val httpClient: HttpClient,
) : CodeTemplatesApi {

    override suspend fun getCodeTemplates(
        limit: Int,
        offset: Int,
    ): NetworkResult<CodeTemplatesDto> = httpClient.safeGet("code-templates?limit=$limit&offset=$offset")

    override suspend fun getCodeTemplate(
        id: String,
    ): NetworkResult<CodeTemplateDto> = httpClient.safeGet("code-templates/$id")
}
