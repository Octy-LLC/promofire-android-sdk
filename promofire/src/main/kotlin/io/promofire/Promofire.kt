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
import io.promofire.models.params.UpdateCodeParams
import io.promofire.models.params.UpdateCustomerParams
import io.promofire.utils.AndroidDeviceSpecsProvider
import io.promofire.utils.EmptyResultCallback
import io.promofire.utils.PromofireResult
import io.promofire.utils.ResultCallback
import java.util.Date
import kotlin.concurrent.Volatile

public object Promofire {

    private var _promofireImpl: PromofireImpl? = null
    private val promofireImpl: PromofireImpl
        get() = _promofireImpl!!

    @Volatile
    public var isInitialized: Boolean = false
        private set

    public var isDebug: Boolean
        get() = Logger.isDebug
        set(value) {
            Logger.isDebug = value
        }

    private val notInitializedError = PromofireResult.Error(
        error = IllegalStateException("Promofire was not initialized")
    )

    @Synchronized
    public fun initialize(context: Context) {
        require(context.applicationContext is Application) { "Application context is required" }

        synchronized(this) {
            if (isInitialized) {
                return
            }

            val storage = PreferencesStorage(context.applicationContext)
            val deviceSpecsProvider = AndroidDeviceSpecsProvider(context.applicationContext)
            _promofireImpl = PromofireImpl(storage, deviceSpecsProvider)
            isInitialized = true
        }
    }

    public fun activate(config: PromofireConfig, callback: EmptyResultCallback) {
        if (!checkInitialized(callback)) return

        promofireImpl.configureSdk(config, callback)
    }

    public fun isCodeGenerationAvailable(callback: ResultCallback<Boolean>) {
        if (checkInitialized(callback)) return

        promofireImpl.isCodeGenerationAvailable(callback)
    }

    public fun getCurrentUserCodes(limit: Int, offset: Int, callback: ResultCallback<Codes>) {
        if (checkInitialized(callback)) return

        promofireImpl.getCurrentUserCodes(limit, offset, callback)
    }

    public fun getCodeByValue(codeValue: String, callback: ResultCallback<Code>) {
        if (checkInitialized(callback)) return

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
        if (checkInitialized(callback)) return

        promofireImpl.getCurrentUserRedeems(limit, offset, from, to, codeValue, callback)
    }

    public fun getCampaigns(limit: Int, offset: Int, callback: ResultCallback<CodeTemplates>) {
        if (checkInitialized(callback)) return

        promofireImpl.getCampaigns(limit, offset, callback)
    }

    public fun getCampaignBy(id: String, callback: ResultCallback<CodeTemplate>) {
        if (checkInitialized(callback)) return

        promofireImpl.getCampaignBy(id, callback)
    }

    public fun generateCode(params: GenerateCodeParams, callback: ResultCallback<Code>) {
        if (checkInitialized(callback)) return

        promofireImpl.generateCode(params, callback)
    }

    public fun generateCodes(params: GenerateCodesParams, callback: ResultCallback<List<Code>>) {
        if (checkInitialized(callback)) return

        promofireImpl.generateCodes(params, callback)
    }

    public fun updateCode(codeValue: String, params: UpdateCodeParams, callback: ResultCallback<Code>) {
        if (checkInitialized(callback)) return

        promofireImpl.updateCode(codeValue, params, callback)
    }

    public fun redeemCode(codeValue: String, callback: EmptyResultCallback) {
        if (checkInitialized(callback)) return

        promofireImpl.redeemCode(codeValue, callback)
    }

    public fun getCurrentUser(callback: ResultCallback<Customer>) {
        if (checkInitialized(callback)) return

        promofireImpl.getCurrentUser(callback)
    }

    public fun updateCurrentUser(params: UpdateCustomerParams, callback: ResultCallback<Customer>) {
        if (checkInitialized(callback)) return

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
        if (checkInitialized(callback)) return

        promofireImpl.getCodeRedeems(limit, offset, from, to, codeValue, redeemerId, callback)
    }

    public fun logout(callback: EmptyResultCallback) {
        if (checkInitialized(callback)) return

        promofireImpl.logout(callback)
    }

    private fun checkInitialized(callback: ResultCallback<*>): Boolean {
        if (!isInitialized) {
            callback.onResult(notInitializedError)
            return false
        }
        return true
    }
}
