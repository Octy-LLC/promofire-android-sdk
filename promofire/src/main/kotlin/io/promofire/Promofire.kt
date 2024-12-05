package io.promofire

import io.promofire.logger.Logger
import kotlin.concurrent.Volatile

public object Promofire {

    @Volatile
    internal var isConfigured: Boolean = false

    public var isDebug: Boolean
        get() = Logger.isDebug
        set(value) {
            Logger.isDebug = value
        }
}
