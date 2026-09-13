package io.promofire.internal

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.promofire.PromofireException
import io.promofire.logger.Logger
import io.promofire.logger.PromofireLogLevel
import io.promofire.promofireError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/** Задержки перед повторами после сетевой ошибки или 5xx. */
private val DEFAULT_BACKOFF_MILLIS = listOf(1_000L, 3_000L)

private const val SUCCESS_RANGE_START = 200
private const val SUCCESS_RANGE_END = 299

internal val PromofireJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * HTTP-слой: повторы, таймауты, подстановка токена и перевод ответов в
 * [PromofireException].
 *
 * Повторы устроены так же, как в других SDK Promofire: `401` однократно
 * запускает тихую переаутентификацию, сетевые сбои и `5xx` повторяются
 * дважды с нарастающей паузой, прочие `4xx` бросаются сразу.
 */
internal class ApiClient(
    private val baseUrl: String,
    timeoutMillis: Long,
    private val userAgent: String,
    private val logger: Logger,
    private val tokenProvider: () -> String?,
    private val reauthenticate: suspend () -> Unit,
    private val backoffMillis: List<Long> = DEFAULT_BACKOFF_MILLIS,
    /** Подменяется в тестах; в бою используется OkHttp. */
    engine: HttpClientEngine? = null,
) {

    private val client = createClient(engine, timeoutMillis)

    private fun createClient(engine: HttpClientEngine?, timeoutMillis: Long) = if (engine == null) {
        HttpClient(OkHttp) { configure(timeoutMillis) }
    } else {
        HttpClient(engine) { configure(timeoutMillis) }
    }

    private fun HttpClientConfig<*>.configure(timeoutMillis: Long) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(PromofireJson)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMillis
            connectTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
        }
    }

    suspend fun request(
        method: HttpMethod,
        path: String,
        query: Map<String, String?> = emptyMap(),
        body: Any? = null,
        anonymous: Boolean = false,
    ): String {
        var reauthenticated = false
        var networkAttempt = 0

        while (true) {
            try {
                return send(method, path, query, body, anonymous)
            } catch (error: PromofireException) {
                when (decide(error, anonymous, reauthenticated, networkAttempt)) {
                    Recovery.REAUTHENTICATE -> {
                        reauthenticated = true
                        logger.log(PromofireLogLevel.INFO, "401 received, re-authenticating")
                        reauthenticate()
                    }

                    Recovery.RETRY -> {
                        val backoff = backoffMillis[networkAttempt]
                        networkAttempt += 1
                        logger.log(
                            PromofireLogLevel.INFO,
                            "${error.code}, retrying in ${backoff}ms " +
                                "(attempt $networkAttempt of ${backoffMillis.size})",
                        )
                        delay(backoff)
                    }

                    Recovery.GIVE_UP -> throw error
                }
            }
        }
    }

    private enum class Recovery { REAUTHENTICATE, RETRY, GIVE_UP }

    /**
     * `401` однократно запускает тихую переаутентификацию, сетевые сбои и
     * `5xx` повторяются с нарастающей паузой, прочие `4xx` бросаются сразу.
     */
    private fun decide(
        error: PromofireException,
        anonymous: Boolean,
        reauthenticated: Boolean,
        networkAttempt: Int,
    ): Recovery {
        val canReauthenticate = error.code == PromofireException.Code.UNAUTHORIZED &&
            !anonymous &&
            !reauthenticated

        if (canReauthenticate) return Recovery.REAUTHENTICATE

        val retriable = error.code == PromofireException.Code.NETWORK_ERROR ||
            error.code == PromofireException.Code.SERVER_ERROR

        return if (retriable && networkAttempt < backoffMillis.size) {
            Recovery.RETRY
        } else {
            Recovery.GIVE_UP
        }
    }

    @Suppress("ThrowsCount")
    private suspend fun send(
        method: HttpMethod,
        path: String,
        query: Map<String, String?>,
        body: Any?,
        anonymous: Boolean,
    ): String {
        val token = when {
            anonymous -> null
            else -> tokenProvider()
                ?: throw promofireError(PromofireException.Code.NOT_AUTHENTICATED)
        }

        logger.log(PromofireLogLevel.INFO, "${method.value} $path")
        if (body != null) logger.log(PromofireLogLevel.DEBUG, "request body: $body")

        val response = try {
            client.request(baseUrl + path) {
                this.method = method
                applyQuery(query)
                header("User-Agent", userAgent)
                token?.let { header("Authorization", "Bearer $it") }
                if (body != null) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (@Suppress("TooGenericExceptionCaught") cause: Exception) {
            throw promofireError(
                code = PromofireException.Code.NETWORK_ERROR,
                message = cause.message ?: "Network request failed",
                cause = cause,
            )
        }

        return handle(response, method, path)
    }

    private suspend fun handle(
        response: HttpResponse,
        method: HttpMethod,
        path: String,
    ): String {
        val raw = response.bodyAsText()
        val status = response.status.value

        logger.log(PromofireLogLevel.INFO, "${method.value} $path -> $status")
        logger.log(PromofireLogLevel.DEBUG, "response body: $raw")

        if (status in SUCCESS_RANGE_START..SUCCESS_RANGE_END) return raw

        val message = extractMessage(raw, "Request failed with status $status")

        throw promofireError(
            code = PromofireException.codeFromStatus(status, message),
            message = message,
            status = status,
        )
    }

    private fun HttpRequestBuilder.applyQuery(query: Map<String, String?>) {
        query.forEach { (key, value) ->
            if (value != null) url.parameters.append(key, value)
        }
    }

    /**
     * Nest отдаёт ошибки как `{ message, error, statusCode }`, причём `message`
     * бывает и строкой, и массивом строк от ValidationPipe.
     */
    private fun extractMessage(raw: String, fallback: String): String {
        if (raw.isBlank()) return fallback

        val element = runCatching { PromofireJson.parseToJsonElement(raw) }.getOrNull()
        val message = (element as? JsonObject)?.get("message") ?: return fallback

        return when (message) {
            is JsonPrimitive -> message.content
            is JsonArray -> message.joinToString("; ") { it.jsonPrimitive.content }
            else -> fallback
        }
    }

    fun close() {
        client.close()
    }
}
