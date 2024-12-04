package io.promofire

import io.promofire.logger.Logger

public object Promofire {

    public var isDebug: Boolean
        get() = Logger.isDebug
        set(value) {
            Logger.isDebug = value
        }
}
