package io.promofire.logger

import android.util.Log

internal class Logger(
    @Volatile var level: PromofireLogLevel = PromofireLogLevel.ERROR,
    /** Куда уходят сообщения. Подменяется в тестах; в бою — `android.util.Log`. */
    private val sink: (PromofireLogLevel, String, Throwable?) -> Unit = ::writeToLogcat,
) {

    fun isEnabled(level: PromofireLogLevel): Boolean =
        level != PromofireLogLevel.NONE && level.ordinal <= this.level.ordinal

    fun log(level: PromofireLogLevel, message: String, throwable: Throwable? = null) {
        if (isEnabled(level)) sink(level, message, throwable)
    }

    /**
     * Строит сообщение, только если уровень включён. Тела запросов и их
     * маскирование недёшевы, а при уровне по умолчанию, ERROR, они не
     * нужны вовсе.
     */
    inline fun log(level: PromofireLogLevel, message: () -> String) {
        if (isEnabled(level)) log(level, message())
    }
}

private const val TAG = "Promofire"

private fun writeToLogcat(level: PromofireLogLevel, message: String, throwable: Throwable?) {
    when (level) {
        PromofireLogLevel.ERROR -> Log.e(TAG, message, throwable)
        PromofireLogLevel.INFO -> Log.i(TAG, message, throwable)
        PromofireLogLevel.DEBUG -> Log.d(TAG, message, throwable)
        PromofireLogLevel.NONE -> Unit
    }
}
