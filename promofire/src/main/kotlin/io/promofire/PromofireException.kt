package io.promofire

/**
 * Единственный тип исключения, который бросает SDK.
 *
 * Разветвляться следует по [code]: он машиночитаем и не меняется вслед за
 * формулировками сообщений с бэкенда.
 */
public class PromofireException internal constructor(
    public val code: Code,
    message: String,
    public val status: Int? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    public enum class Code {
        /** Не вызван [Promofire.configure]. */
        NOT_CONFIGURED,

        /** Нет активной сессии: не вызван [Promofire.connect] или [Promofire.identify]. */
        NOT_AUTHENTICATED,

        /** Токен недействителен или истёк. SDK уже попытался переаутентифицироваться. */
        UNAUTHORIZED,

        /** Операция запрещена: например, шаблон недоступен клиентам или код чужой. */
        FORBIDDEN,

        /** Ресурс не найден. */
        NOT_FOUND,

        /** Ресурс уже существует: например, код с таким значением занят. */
        CONFLICT,

        /** Срок действия кода истёк. */
        CODE_EXPIRED,

        /** Погашения кода исчерпаны. */
        CODE_FULLY_REDEEMED,

        /** Некорректные параметры запроса. */
        VALIDATION_ERROR,

        /** Сбой связи или таймаут. */
        NETWORK_ERROR,

        /** Непредвиденная ошибка на сервере. */
        SERVER_ERROR,

        /** Всё остальное. */
        UNKNOWN,
    }

    internal companion object {

        /**
         * Сопоставляет HTTP-статус с кодом ошибки.
         *
         * `410` бэкенд отдаёт и на истёкший код, и на полностью погашенный,
         * различить их можно только по тексту сообщения. При изменении
         * формулировок в `CodeExpiredException` и `CodeFullyRedeemedException`
         * на бэкенде нужно поправить и здесь.
         */
        fun codeFromStatus(status: Int, message: String): Code = when (status) {
            HTTP_BAD_REQUEST -> Code.VALIDATION_ERROR
            HTTP_UNAUTHORIZED -> Code.UNAUTHORIZED
            HTTP_FORBIDDEN -> Code.FORBIDDEN
            HTTP_NOT_FOUND -> Code.NOT_FOUND
            HTTP_CONFLICT -> Code.CONFLICT
            HTTP_GONE -> if (message.contains(FULLY_REDEEMED_MARKER, ignoreCase = true)) {
                Code.CODE_FULLY_REDEEMED
            } else {
                Code.CODE_EXPIRED
            }
            in HTTP_SERVER_ERROR_RANGE -> Code.SERVER_ERROR
            else -> Code.UNKNOWN
        }

        private const val HTTP_BAD_REQUEST = 400
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_CONFLICT = 409
        private const val HTTP_GONE = 410
        private val HTTP_SERVER_ERROR_RANGE = 500..599
        private const val FULLY_REDEEMED_MARKER = "fully redeemed"
    }
}

internal fun promofireError(
    code: PromofireException.Code,
    message: String? = null,
    status: Int? = null,
    cause: Throwable? = null,
): PromofireException = PromofireException(
    code = code,
    message = message ?: defaultMessage(code),
    status = status,
    cause = cause,
)

private fun defaultMessage(code: PromofireException.Code): String = when (code) {
    PromofireException.Code.NOT_CONFIGURED -> "Promofire is not configured. Call configure() first."
    PromofireException.Code.NOT_AUTHENTICATED -> "No active session. Call connect() or identify() first."
    PromofireException.Code.UNAUTHORIZED -> "Invalid or expired token."
    PromofireException.Code.FORBIDDEN -> "Operation is not allowed."
    PromofireException.Code.NOT_FOUND -> "Resource not found."
    PromofireException.Code.CONFLICT -> "Resource already exists."
    PromofireException.Code.CODE_EXPIRED -> "Code has expired."
    PromofireException.Code.CODE_FULLY_REDEEMED -> "Code has been fully redeemed."
    PromofireException.Code.VALIDATION_ERROR -> "Invalid request parameters."
    PromofireException.Code.NETWORK_ERROR -> "Network request failed."
    PromofireException.Code.SERVER_ERROR -> "Unexpected server error."
    PromofireException.Code.UNKNOWN -> "Unexpected error."
}
