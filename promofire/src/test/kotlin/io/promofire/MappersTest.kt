package io.promofire

import io.promofire.internal.CodeDto
import io.promofire.internal.CodeRedeemsDto
import io.promofire.internal.CodeTemplatesDto
import io.promofire.internal.CodesDto
import io.promofire.internal.PromofireJson
import io.promofire.internal.toModel
import io.promofire.models.Code
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private val codeJson = """
        {
          "value": "SUMMER",
          "status": "ACTIVE",
          "templateId": "t-1",
          "createdAt": "2026-09-01T00:00:00.000Z",
          "updatedAt": "2026-09-02T00:00:00.000Z",
          "expiresAt": 1789000000000,
          "ownerId": "o-1",
          "payload": { "discount": "10" },
          "amount": "Infinity"
        }
    """.trimIndent()

    @Test
    fun `код разбирается полностью`() {
        val code = PromofireJson.decodeFromString<CodeDto>(codeJson).toModel()

        assertEquals("SUMMER", code.value)
        assertEquals(Code.Status.ACTIVE, code.status)
        assertEquals(mapOf("discount" to "10"), code.payload)
        assertEquals("Infinity", code.amount)
        assertEquals(1789000000000L, code.expiresAt)
    }

    @Test
    fun `статус EXPIRED разбирается, а не роняет ответ`() {
        // В версии 0.2.0 такого значения в enum не было, и kotlinx.serialization
        // бросал исключение на любом ответе, содержащем истёкший код.
        val expired = codeJson.replace("\"ACTIVE\"", "\"EXPIRED\"")

        val code = PromofireJson.decodeFromString<CodeDto>(expired).toModel()

        assertEquals(Code.Status.EXPIRED, code.status)
    }

    @Test
    fun `неизвестные поля в ответе не мешают`() {
        val extended = codeJson.replace("\"value\": \"SUMMER\",", "\"value\": \"SUMMER\", \"brandNewField\": 1,")

        assertEquals("SUMMER", PromofireJson.decodeFromString<CodeDto>(extended).value)
    }

    @Test
    fun `codes приводится к items`() {
        val json = """{ "codes": [$codeJson], "total": 1 }"""

        val page = PromofireJson.decodeFromString<CodesDto>(json).toModel()

        assertEquals(1, page.total)
        assertEquals(1, page.items.size)
        assertEquals("SUMMER", page.items.first().value)
    }

    @Test
    fun `redeems приводится к items`() {
        val json = """
            {
              "redeems": [{
                "id": "r-1",
                "redeemerId": "c-1",
                "code": "SUMMER",
                "platform": "ANDROID",
                "country": "UA",
                "templateId": "t-1",
                "redeemedAt": "2026-09-03T10:00:00.000Z"
              }],
              "total": 1
            }
        """.trimIndent()

        val page = PromofireJson.decodeFromString<CodeRedeemsDto>(json).toModel()

        assertEquals(1, page.items.size)
        assertEquals("SUMMER", page.items.first().code)
    }

    @Test
    fun `templates приводится к items`() {
        val json = """
            {
              "templates": [{
                "id": "t-1",
                "name": "Campaign",
                "creatorId": "e-1",
                "status": "ACTIVE",
                "createdAt": "2026-09-01T00:00:00.000Z",
                "ttl": 2592000000,
                "amount": "5",
                "hasMutablePayload": true,
                "isUsableByCustomers": true,
                "description": null,
                "payload": {}
              }],
              "total": 1
            }
        """.trimIndent()

        val page = PromofireJson.decodeFromString<CodeTemplatesDto>(json).toModel()

        assertEquals(1, page.items.size)
        assertTrue(page.items.first().isUsableByCustomers)
        assertEquals(emptyMap<String, String>(), page.items.first().payload)
    }

    @Test
    fun `пустой список не ломается`() {
        val page = PromofireJson.decodeFromString<CodesDto>("""{ "total": 0 }""").toModel()

        assertEquals(0, page.total)
        assertTrue(page.items.isEmpty())
    }
}
