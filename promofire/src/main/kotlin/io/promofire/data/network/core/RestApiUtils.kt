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
import io.promofire.data.network.core.exceptions.PromofireNetworkException
import kotlinx.coroutines.CancellationException

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
    else -> NetworkResult.Error(body<PromofireNetworkException>())
}
