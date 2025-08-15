package io.promofire

import android.app.Application
import android.content.Context
import io.promofire.config.PromofireConfig
import io.promofire.data.local.PreferencesStorage
import io.promofire.logger.Logger
import io.promofire.models.Code
import io.promofire.models.CodeRedeems
import io.promofire.models.CodeTemplate
import io.promofire.models.CodeTemplates
import io.promofire.models.Codes
import io.promofire.models.Customer
import io.promofire.models.params.GenerateCodeParams
import io.promofire.models.params.GenerateCodesParams
import io.promofire.models.params.UpdateCustomerParams
import io.promofire.utils.AndroidDeviceSpecsProvider
import io.promofire.utils.EmptyResultCallback
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

    public fun configure(context: Context, config: PromofireConfig, callback: EmptyResultCallback) {
        require(context.applicationContext is Application) { "Application context is required" }

        isConfigured = true
        val storage = PreferencesStorage(context.applicationContext)
        val deviceSpecsProvider = AndroidDeviceSpecsProvider(context.applicationContext)
        promofireImpl.configureSdk(config, storage, deviceSpecsProvider, callback)
    }

    public fun isCodeGenerationAvailable(callback: ResultCallback<Boolean>) {
        promofireImpl.isCodeGenerationAvailable(callback)
    }

    public fun getCurrentUserCodes(limit: Int, offset: Int, callback: ResultCallback<Codes>) {
        promofireImpl.getCurrentUserCodes(limit, offset, callback)
    }

    public fun getCodeByValue(codeValue: String, callback: ResultCallback<Code>) {
        promofireImpl.getCodeByValue(codeValue, callback)
    }

    public fun getCurrentUserRedeems(
        limit: Int,
        offset: Int,
        from: Date,
        to: Date,
        codeValue: String? = null,
        callback: ResultCallback<CodeRedeems>,
    ) {
        promofireImpl.getCurrentUserRedeems(limit, offset, from, to, codeValue, callback)
    }

    public fun getCampaigns(limit: Int, offset: Int, callback: ResultCallback<CodeTemplates>) {
        promofireImpl.getCampaigns(limit, offset, callback)
    }

    public fun getCampaignBy(id: String, callback: ResultCallback<CodeTemplate>) {
        promofireImpl.getCampaignBy(id, callback)
    }

    public fun generateCode(params: GenerateCodeParams, callback: ResultCallback<Code>) {
        promofireImpl.generateCode(params, callback)
    }

    public fun generateCodes(params: GenerateCodesParams, callback: ResultCallback<List<Code>>) {
        promofireImpl.generateCodes(params, callback)
    }

    public fun redeemCode(codeValue: String, callback: EmptyResultCallback) {
        promofireImpl.redeemCode(codeValue, callback)
    }

    public fun getCurrentUser(callback: ResultCallback<Customer>) {
        promofireImpl.getCurrentUser(callback)
    }

    public fun updateCurrentUser(params: UpdateCustomerParams, callback: ResultCallback<Customer>) {
        promofireImpl.updateCurrentUser(params, callback)
    }

    public fun getCodeRedeems(
        limit: Int,
        offset: Int,
        from: Date,
        to: Date,
        codeValue: String? = null,
        redeemerId: String? = null,
        callback: ResultCallback<CodeRedeems>,
    ) {
        promofireImpl.getCodeRedeems(limit, offset, from, to, codeValue, redeemerId, callback)
    }

    public fun logout(callback: EmptyResultCallback) {
        promofireImpl.logout(callback)
    }
}
