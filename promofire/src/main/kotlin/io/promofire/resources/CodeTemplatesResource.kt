package io.promofire.resources

import io.ktor.http.HttpMethod
import io.promofire.internal.ApiClient
import io.promofire.internal.CodeTemplateDto
import io.promofire.internal.CodeTemplatesDto
import io.promofire.internal.PromofireJson
import io.promofire.internal.toModel
import io.promofire.models.CodeTemplate
import io.promofire.models.PaginatedList

public class CodeTemplatesResource internal constructor(
    private val api: ApiClient,
) {

    /** Кампании, доступные текущему клиенту. */
    public suspend fun list(limit: Int, offset: Int? = null): PaginatedList<CodeTemplate> {
        val raw = api.request(
            method = HttpMethod.Get,
            path = "/code-templates",
            query = mapOf("limit" to limit.toString(), "offset" to offset?.toString()),
        )

        return PromofireJson.decodeFromString<CodeTemplatesDto>(raw).toModel()
    }

    public suspend fun get(id: String): CodeTemplate {
        val raw = api.request(HttpMethod.Get, "/code-templates/$id")

        return PromofireJson.decodeFromString<CodeTemplateDto>(raw).toModel()
    }
}
