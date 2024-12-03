package io.promofire.logger

import android.util.Log

internal class AndroidConsoleLogger : Logger {

    companion object {
        private const val TAG = "Promofire"
    }

    override fun log(level: PromofireLogLevel, message: String, throwable: Throwable?) {
        when (level) {
            PromofireLogLevel.VERBOSE -> Log.v(TAG, message, throwable)
            PromofireLogLevel.INFO -> Log.i(TAG, message, throwable)
            PromofireLogLevel.WARNING -> Log.w(TAG, message, throwable)
            PromofireLogLevel.ERROR -> Log.e(TAG, message, throwable)
        }
    }
}
