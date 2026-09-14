package io.promofire.logger

import android.util.Log

internal class Logger(
    @Volatile var level: PromofireLogLevel = PromofireLogLevel.ERROR,
) {

    fun log(level: PromofireLogLevel, message: String, throwable: Throwable? = null) {
        if (level == PromofireLogLevel.NONE || level.ordinal > this.level.ordinal) return

        when (level) {
            PromofireLogLevel.ERROR -> Log.e(TAG, message, throwable)
            PromofireLogLevel.INFO -> Log.i(TAG, message, throwable)
            PromofireLogLevel.DEBUG -> Log.d(TAG, message, throwable)
            PromofireLogLevel.NONE -> Unit
        }
    }

    private companion object {
        const val TAG = "Promofire"
    }
}
