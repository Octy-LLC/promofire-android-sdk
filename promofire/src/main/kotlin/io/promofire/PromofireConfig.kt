package io.promofire

import io.promofire.logger.PromofireLogLevel

/**
 * Настройки SDK.
 *
 * Платформа здесь не задаётся: эта библиотека работает только на Android,
 * и подставлять её должен не хост. Ошибиться платформой было главным
 * источником невнятного `404` при аутентификации.
 */
public data class PromofireConfig(
    /** SDK-секрет из панели: 64 шестнадцатеричных символа, выпущенный для Android. */
    val secret: String,
    /** Адрес API. По умолчанию продакшен; переопределяется для стейджа и локальной разработки. */
    val baseUrl: String = DEFAULT_BASE_URL,
    /**
     * По умолчанию [PromofireLogLevel.ERROR], а при переопределённом [baseUrl] —
     * [PromofireLogLevel.DEBUG]. Уровень `DEBUG` печатает тела запросов и ответов:
     * там бывают персональные данные, в продакшене его включать не стоит.
     * SDK-секрет и токен клиента маскируются.
     */
    val logLevel: PromofireLogLevel? = null,
    /** Таймаут запроса в миллисекундах. */
    val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) {

    public companion object {
        public const val DEFAULT_BASE_URL: String = "https://api.promofire.io"
        public const val DEFAULT_TIMEOUT_MILLIS: Long = 30_000
    }
}
