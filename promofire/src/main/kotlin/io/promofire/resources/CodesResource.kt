package io.promofire.resources

import io.ktor.http.HttpMethod
import io.promofire.internal.ApiClient
import io.promofire.internal.CodeDto
import io.promofire.internal.CodeRedeemsDto
import io.promofire.internal.CodesDto
import io.promofire.internal.CreateCodeRequestDto
import io.promofire.internal.CreateCodesRequestDto
import io.promofire.internal.PromofireJson
import io.promofire.internal.RedeemRequestDto
import io.promofire.internal.UpdateCodeRequestDto
import io.promofire.internal.toModel
import io.promofire.models.Code
import io.promofire.models.CodeRedeem
import io.promofire.models.PaginatedList
import io.promofire.models.Platform
import io.promofire.models.params.CreateCodeParams
import io.promofire.models.params.CreateCodesParams
import io.promofire.models.params.UpdateCodeParams
import io.promofire.utils.toIsoString
import java.util.Date

public class CodesResource internal constructor(
    private val api: ApiClient,
) {

    /** Коды, которыми владеет текущий клиент. */
    public suspend fun getMine(limit: Int, offset: Int? = null): PaginatedList<Code> {
        val raw = api.request(
            method = HttpMethod.Get,
            path = "/codes/me",
            query = mapOf("limit" to limit.toString(), "offset" to offset?.toString()),
        )

        return PromofireJson.decodeFromString<CodesDto>(raw).toModel()
    }

    public suspend fun get(codeValue: String): Code {
        val raw = api.request(HttpMethod.Get, "/codes/${codeValue.encode()}")

        return PromofireJson.decodeFromString<CodeDto>(raw).toModel()
    }

    /**
     * Погашает код. Платформу SDK подставляет сам: бэкенд требует её
     * обязательно, а без неё разъезжается аналитика по платформам.
     *
     * Бросает `NOT_FOUND`, `CODE_EXPIRED` или `CODE_FULLY_REDEEMED`.
     */
    public suspend fun redeem(codeValue: String) {
        api.request(
            method = HttpMethod.Post,
            path = "/codes/redeem",
            body = RedeemRequestDto(codeValue = codeValue, platform = Platform.ANDROID),
        )
    }

    /** История погашений текущего клиента. */
    public suspend fun getMyRedeems(
        limit: Int,
        from: Date,
        to: Date,
        offset: Int? = null,
        codeValue: String? = null,
    ): PaginatedList<CodeRedeem> {
        val raw = api.request(
            method = HttpMethod.Get,
            path = "/codes/redeems/me",
            query = mapOf(
                "limit" to limit.toString(),
                "offset" to offset?.toString(),
                "from" to from.toIsoString(),
                "to" to to.toIsoString(),
                "codeValue" to codeValue,
            ),
        )

        return PromofireJson.decodeFromString<CodeRedeemsDto>(raw).toModel()
    }

    /**
     * Выпускает код из шаблона, помеченного `isUsableByCustomers`.
     *
     * Владельца проставляет сервер. Срок жизни и число погашений наследуются
     * от шаблона и здесь не задаются.
     */
    public suspend fun create(params: CreateCodeParams): Code {
        val raw = api.request(
            method = HttpMethod.Post,
            path = "/codes",
            body = CreateCodeRequestDto(
                value = params.value,
                templateId = params.templateId,
                payload = params.payload,
            ),
        )

        return PromofireJson.decodeFromString<CodeDto>(raw).toModel()
    }

    /** Выпускает несколько кодов сразу. Значения генерирует сервер. */
    public suspend fun createBatch(params: CreateCodesParams): List<Code> {
        val raw = api.request(
            method = HttpMethod.Post,
            path = "/codes/batch",
            body = CreateCodesRequestDto(
                templateId = params.templateId,
                payload = params.payload,
                count = params.count,
            ),
        )

        return PromofireJson.decodeFromString<List<CodeDto>>(raw).map(CodeDto::toModel)
    }

    /** Меняет `payload`, если шаблон это разрешает, и включает или отключает код. */
    public suspend fun update(codeValue: String, params: UpdateCodeParams): Code {
        val raw = api.request(
            method = HttpMethod.Patch,
            path = "/codes/${codeValue.encode()}",
            body = UpdateCodeRequestDto(payload = params.payload, active = params.active),
        )

        return PromofireJson.decodeFromString<CodeDto>(raw).toModel()
    }
}

private fun String.encode(): String = java.net.URLEncoder.encode(this, "UTF-8")
