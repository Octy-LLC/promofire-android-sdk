package io.promofire.logger

import io.promofire.logger.PromofireLogLevel.ERROR
import io.promofire.logger.PromofireLogLevel.INFO
import io.promofire.logger.PromofireLogLevel.VERBOSE
import io.promofire.logger.PromofireLogLevel.WARNING

internal interface Logger {

    companion object : Logger by AndroidConsoleLogger()

    var isDebug: Boolean

    fun log(level: PromofireLogLevel, message: String, throwable: Throwable? = null)
}

internal fun Logger.Companion.v(message: String) {
    Logger.log(VERBOSE, message)
}

internal fun Logger.Companion.i(message: String) {
    Logger.log(INFO, message)
}

internal fun Logger.Companion.w(message: String, throwable: Throwable? = null) {
    Logger.log(WARNING, message, throwable)
}

internal fun Logger.Companion.e(message: String, throwable: Throwable? = null) {
    Logger.log(ERROR, message, throwable)
}
