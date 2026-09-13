package io.promofire

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.promofire.internal.ApiClient
import io.promofire.logger.Logger
import io.promofire.logger.PromofireLogLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val JSON_HEADERS = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

class ApiClientTest {

    private var calls = 0
    private var reauths = 0

    private fun clientOver(
        vararg responses: Pair<HttpStatusCode, String>,
        token: String? = "token",
    ): ApiClient {
        calls = 0
        reauths = 0

        val engine = MockEngine { _ ->
            val index = calls
            calls += 1
            val (status, body) = responses.getOrNull(index)
                ?: error("unexpected extra request #${index + 1}")
            respond(body, status, JSON_HEADERS)
        }

        return ApiClient(
            baseUrl = "https://api.test",
            timeoutMillis = 1_000,
            userAgent = "PromofireSDK/test",
            logger = Logger(PromofireLogLevel.NONE),
            tokenProvider = { token },
            reauthenticate = { reauths += 1 },
            backoffMillis = listOf(1, 1),
            engine = engine,
        )
    }


    @Test
    fun `повторяет 5xx дважды и отдаёт SERVER_ERROR`() = runTest {
        val client = clientOver(
            HttpStatusCode.InternalServerError to """{"message":"boom"}""",
            HttpStatusCode.BadGateway to """{"message":"boom"}""",
            HttpStatusCode.ServiceUnavailable to """{"message":"boom"}""",
        )

        val error = runCatching { client.request(HttpMethod.Get, "/x") }.exceptionOrNull()

        assertTrue(error is PromofireException)
        assertEquals(PromofireException.Code.SERVER_ERROR, (error as PromofireException).code)
        assertEquals("один исходный запрос и два повтора", 3, calls)
    }

    @Test
    fun `перестаёт повторять после успеха`() = runTest {
        val client = clientOver(
            HttpStatusCode.InternalServerError to """{"message":"boom"}""",
            HttpStatusCode.OK to """{"ok":true}""",
        )

        assertEquals("""{"ok":true}""", client.request(HttpMethod.Get, "/x"))
        assertEquals(2, calls)
    }

    @Test
    fun `4xx не повторяется`() = runTest {
        val client = clientOver(HttpStatusCode.BadRequest to """{"message":"bad"}""")

        val error = runCatching { client.request(HttpMethod.Get, "/x") }
            .exceptionOrNull() as PromofireException

        assertEquals(PromofireException.Code.VALIDATION_ERROR, error.code)
        assertEquals(400, error.status)
        assertEquals(1, calls)
    }

    @Test
    fun `401 переаутентифицирует один раз и повторяет запрос`() = runTest {
        val client = clientOver(
            HttpStatusCode.Unauthorized to """{"message":"nope"}""",
            HttpStatusCode.OK to """{"ok":true}""",
        )

        assertEquals("""{"ok":true}""", client.request(HttpMethod.Get, "/x"))
        assertEquals(1, reauths)
        assertEquals(2, calls)
    }

    @Test
    fun `второй подряд 401 больше не переаутентифицирует`() = runTest {
        val client = clientOver(
            HttpStatusCode.Unauthorized to """{"message":"nope"}""",
            HttpStatusCode.Unauthorized to """{"message":"nope"}""",
        )

        val error = runCatching { client.request(HttpMethod.Get, "/x") }
            .exceptionOrNull() as PromofireException

        assertEquals(PromofireException.Code.UNAUTHORIZED, error.code)
        assertEquals("ровно одна попытка, иначе цикл", 1, reauths)
    }

    @Test
    fun `анонимный запрос не переаутентифицируется`() = runTest {
        val client = clientOver(
            HttpStatusCode.Unauthorized to """{"message":"nope"}""",
            token = null,
        )

        val error = runCatching {
            client.request(HttpMethod.Post, "/auth/sdk/customer", anonymous = true)
        }.exceptionOrNull() as PromofireException

        assertEquals(PromofireException.Code.UNAUTHORIZED, error.code)
        assertEquals(0, reauths)
    }

    @Test
    fun `без токена запрос не уходит`() = runTest {
        val client = clientOver(token = null)

        val error = runCatching { client.request(HttpMethod.Get, "/x") }
            .exceptionOrNull() as PromofireException

        assertEquals(PromofireException.Code.NOT_AUTHENTICATED, error.code)
        assertEquals("сетевого вызова не было", 0, calls)
    }

    @Test
    fun `410 различает истёкший и полностью погашенный код`() = runTest {
        val expired = clientOver(
            HttpStatusCode.Gone to """{"message":"Code with value 'A' expired"}""",
        )
        assertEquals(
            PromofireException.Code.CODE_EXPIRED,
            (runCatching { expired.request(HttpMethod.Post, "/codes/redeem") }
                .exceptionOrNull() as PromofireException).code,
        )

        val drained = clientOver(
            HttpStatusCode.Gone to """{"message":"Code with value 'A' fully redeemed"}""",
        )
        assertEquals(
            PromofireException.Code.CODE_FULLY_REDEEMED,
            (runCatching { drained.request(HttpMethod.Post, "/codes/redeem") }
                .exceptionOrNull() as PromofireException).code,
        )
    }

    @Test
    fun `склеивает массив сообщений от ValidationPipe`() = runTest {
        val client = clientOver(
            HttpStatusCode.BadRequest to """{"message":["limit must be a positive integer","bad offset"]}""",
        )

        val error = runCatching { client.request(HttpMethod.Get, "/x") }
            .exceptionOrNull() as PromofireException

        assertEquals("limit must be a positive integer; bad offset", error.message)
    }

    @Test
    fun `пустое тело не ломает разбор`() = runTest {
        val client = clientOver(HttpStatusCode.NoContent to "")

        assertEquals("", client.request(HttpMethod.Post, "/codes/redeem"))
    }

    @Test
    fun `не подставляет null в строку запроса`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("limit=10&codeValue=X", request.url.encodedQuery)
            respond("""{"ok":true}""", HttpStatusCode.OK, JSON_HEADERS)
        }

        val client = ApiClient(
            baseUrl = "https://api.test",
            timeoutMillis = 1_000,
            userAgent = "PromofireSDK/test",
            logger = Logger(PromofireLogLevel.NONE),
            tokenProvider = { "token" },
            reauthenticate = {},
            engine = engine,
        )

        client.request(
            method = HttpMethod.Get,
            path = "/codes/me",
            query = mapOf("limit" to "10", "offset" to null, "codeValue" to "X"),
        )
    }
}
