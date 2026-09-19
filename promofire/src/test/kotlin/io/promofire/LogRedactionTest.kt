package io.promofire

import io.promofire.internal.AuthRequestDto
import io.promofire.internal.redactCredentials
import io.promofire.models.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogRedactionTest {

    private val secret = "a".repeat(64)

    @Test
    fun `секрет в toString запроса аутентификации маскируется`() {
        val request = AuthRequestDto(
            secret = secret,
            platform = Platform.ANDROID,
            device = "Pixel",
            os = "Android 15",
            appBuild = "1",
            appVersion = "1.0.0",
            sdkVersion = "1.0.0-beta.1",
        )

        val logged = redactCredentials(request.toString())

        assertFalse(logged.contains(secret))
        assertTrue("остальное тело на месте", logged.contains("platform=ANDROID"))
        assertTrue(logged.contains("secret=<redacted>"))
    }

    @Test
    fun `токен в JSON ответа маскируется`() {
        val logged = redactCredentials("""{"accessToken":"eyJhbGciOi.payload.sig","extra":1}""")

        assertEquals("""{"accessToken":"<redacted>","extra":1}""", logged)
    }

    @Test
    fun `секрет в JSON маскируется и с пробелами вокруг двоеточия`() {
        val logged = redactCredentials("""{ "secret" : "$secret", "platform": "ANDROID" }""")

        assertFalse(logged.contains(secret))
        assertTrue(logged.contains(""""platform": "ANDROID""""))
    }

    @Test
    fun `тело без учётных данных не меняется`() {
        val body = """{"value":"SUMMER","payload":{"discount":"10"}}"""

        assertEquals(body, redactCredentials(body))
    }
}
