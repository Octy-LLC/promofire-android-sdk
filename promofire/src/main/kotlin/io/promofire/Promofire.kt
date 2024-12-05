package io.promofire

import android.app.Application
import android.content.Context
import io.promofire.config.PromofireConfig
import io.promofire.logger.Logger
import io.promofire.utils.AndroidDeviceSpecsProvider
import kotlin.concurrent.Volatile

public object Promofire {

    private val promofireImpl = PromofireImpl()

    @Volatile
    internal var isConfigured: Boolean = false

    public var isDebug: Boolean
        get() = Logger.isDebug
        set(value) {
            Logger.isDebug = value
        }

    public fun configure(context: Context, config: PromofireConfig) {
        require(!isConfigured) { "Promofire is already configured" }
        require(context.applicationContext is Application) { "Application context is required" }

        isConfigured = true
        val deviceSpecsProvider = AndroidDeviceSpecsProvider(context.applicationContext)
        promofireImpl.configureSdk(config, deviceSpecsProvider)
    }
}
