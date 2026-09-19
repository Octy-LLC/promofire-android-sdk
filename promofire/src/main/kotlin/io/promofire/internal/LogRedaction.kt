package io.promofire.internal

private const val REDACTED = "<redacted>"

private val CREDENTIAL_PATTERNS = listOf(
    // toString data-класса: AuthRequestDto(secret=…, platform=…)
    Regex("""\b((?:secret|accessToken)=)[^,)\s]+"""),
    // JSON: "secret":"…", "accessToken":"…"
    Regex("""("(?:secret|accessToken)"\s*:\s*")[^"]*"""),
)

/**
 * Маскирует учётные данные в строке, которая уходит в журнал.
 *
 * Уровень DEBUG печатает тела запросов и ответов, а среди них запрос
 * аутентификации с SDK-секретом и ответ с токеном клиента. DEBUG включается
 * сам для любого нестандартного `baseUrl`, так что эти строки оказывались в
 * logcat у каждого, кто подключался к стейджу, а оттуда — в баг-репортах.
 *
 * Журнал нужен ради тел, поэтому вырезаются только значения этих двух полей.
 * Тело попадает в журнал в двух видах, оба покрыты: `toString` data-класса
 * для запроса и сырой JSON для ответа.
 */
internal fun redactCredentials(text: String): String =
    CREDENTIAL_PATTERNS.fold(text) { acc, pattern ->
        pattern.replace(acc) { match -> match.groupValues[1] + REDACTED }
    }
