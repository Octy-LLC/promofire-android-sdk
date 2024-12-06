package io.promofire

import android.app.Application
import android.content.Context
import io.promofire.config.PromofireConfig
import io.promofire.logger.Logger
import io.promofire.models.Code
import io.promofire.models.CodeRedeems
import io.promofire.models.CodeTemplate
import io.promofire.models.CodeTemplates
import io.promofire.models.Codes
import io.promofire.models.params.GenerateCodeParams
import io.promofire.utils.AndroidDeviceSpecsProvider
import io.promofire.utils.ResultCallback
import java.util.Date
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

    public fun isCodeGenerationAvailable(callback: ResultCallback<Boolean>) {
        checkIsConfigured()

        promofireImpl.isCodeGenerationAvailable(callback)
    }

    public fun getCurrentUserCodes(limit: Int, offset: Int, callback: ResultCallback<Codes>) {
        checkIsConfigured()

        promofireImpl.getCurrentUserCodes(limit, offset, callback)
    }

    public fun getCurrentUserRedeems(
        limit: Int,
        offset: Int,
        from: Date,
        to: Date,
        callback: ResultCallback<CodeRedeems>,
    ) {
        checkIsConfigured()

        promofireImpl.getCurrentUserRedeems(limit, offset, from, to, callback)
    }

    public fun getCampaigns(limit: Int, offset: Int, callback: ResultCallback<CodeTemplates>) {
        checkIsConfigured()

        promofireImpl.getCampaigns(limit, offset, callback)
    }

    public fun getCampaignBy(id: String, callback: ResultCallback<CodeTemplate>) {
        checkIsConfigured()

        promofireImpl.getCampaignBy(id, callback)
    }

    public fun generateCode(params: GenerateCodeParams, callback: ResultCallback<Code>) {
        checkIsConfigured()

        promofireImpl.generateCode(params, callback)
    }

    private fun checkIsConfigured() {
        require(isConfigured) { "Promofire is not configured" }
    }
}
