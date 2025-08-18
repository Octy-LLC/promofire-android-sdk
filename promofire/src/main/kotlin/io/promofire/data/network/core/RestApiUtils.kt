package io.promofire.data.network.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.promofire.data.network.core.serializers.StringListSerializer
import io.promofire.models.exceptions.PromofireNetworkException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

internal suspend inline fun <reified Request, reified Response> HttpClient.safePost(
    endpoint: String,
    body: Request,
): NetworkResult<Response> = safeCall { post(endpoint) { setBody(body) } }.body()

internal suspend inline fun <reified Request, reified Response> HttpClient.safePut(
    endpoint: String,
    body: Request,
): NetworkResult<Response> = safeCall { put(endpoint) { setBody(body) } }.body()

internal suspend inline fun <reified Request, reified Response> HttpClient.safePatch(
    endpoint: String,
    body: Request,
): NetworkResult<Response> = safeCall { patch(endpoint) { setBody(body) } }.body()

internal suspend inline fun <reified Response> HttpClient.safeGet(
    endpoint: String,
    crossinline block: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<Response> = safeCall { get(endpoint, block) }.body()

private suspend fun safeCall(block: suspend () -> HttpResponse): NetworkResult<HttpResponse> = try {
    NetworkResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
    NetworkResult.Error(e)
}

private suspend inline fun <reified T> NetworkResult<HttpResponse>.body(): NetworkResult<T> = when (this) {
    is NetworkResult.Success -> data.safeBody()
    is NetworkResult.Error -> this
}

private suspend inline fun <reified T> HttpResponse.safeBody(): NetworkResult<T> = when (status.value) {
    in 200..299 -> NetworkResult.Success(body())
    else -> {
        val exceptionDto = body<PromofireNetworkExceptionDto>()
        NetworkResult.Error(exceptionDto.toDomain())
    }
}

@Serializable
internal class PromofireNetworkExceptionDto(
    @Serializable(StringListSerializer::class)
    val messages: List<String>? = null,
    @Serializable(StringListSerializer::class)
    val message: List<String>? = null,
    val error: String? = null,
    val statusCode: Int,
)

private fun PromofireNetworkExceptionDto.toDomain(): PromofireNetworkException = PromofireNetworkException(
    message = message?.joinToString(separator = " | ") ?: messages?.joinToString(separator = " | "),
    error = error,
    statusCode = statusCode,
)
