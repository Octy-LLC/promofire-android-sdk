package io.promofire.logger

import android.util.Log

internal class AndroidConsoleLogger : Logger {

    companion object {
        private const val TAG = "Promofire"
    }

    override var isDebug: Boolean = false

    override fun log(level: PromofireLogLevel, message: String, throwable: Throwable?) {
        if (!isDebug) return
        when (level) {
            PromofireLogLevel.VERBOSE -> Log.v(TAG, message, throwable)
            PromofireLogLevel.INFO -> Log.i(TAG, message, throwable)
            PromofireLogLevel.WARNING -> Log.w(TAG, message, throwable)
            PromofireLogLevel.ERROR -> Log.e(TAG, message, throwable)
        }
    }
}
