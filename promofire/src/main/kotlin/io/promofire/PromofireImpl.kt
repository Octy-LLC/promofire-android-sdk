package io.promofire

import io.promofire.config.PromofireConfig
import io.promofire.interactors.CodeGenerationInteractor
import io.promofire.interactors.CodeTemplatesInteractor
import io.promofire.interactors.CodesInteractor
import io.promofire.interactors.CustomerInteractor
import io.promofire.logger.Logger
import io.promofire.logger.e
import io.promofire.models.Code
import io.promofire.models.CodeRedeems
import io.promofire.models.CodeTemplate
import io.promofire.models.CodeTemplates
import io.promofire.models.Codes
import io.promofire.models.Customer
import io.promofire.models.params.GenerateCodeParams
import io.promofire.models.params.GenerateCodesParams
import io.promofire.models.params.UpdateCustomerParams
import io.promofire.utils.DeviceSpecsProvider
import io.promofire.utils.ErrorCallback
import io.promofire.utils.PromofireResult
import io.promofire.utils.ResultCallback
import io.promofire.utils.promofireScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

internal class PromofireImpl {

    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    private val promofireConfigurator = PromofireConfigurator()

    private var configurationJob: Job? = null
        get() = try {
            lock.readLock().lock()
            field
        } finally {
            lock.readLock().unlock()
        }
        set(value) = try {
            lock.writeLock().lock()
            field = value
        } finally {
            lock.writeLock().unlock()
        }

    private var isCodeGenerationAvailable: Boolean = false
        get() = try {
            lock.readLock().lock()
            field
        } finally {
            lock.readLock().unlock()
        }
        set(value) = try {
            lock.writeLock().lock()
            field = value
        } finally {
            lock.writeLock().unlock()
        }

    private val codeGenerationInteractor by lazy {
        CodeGenerationInteractor()
    }

    private val codesInteractor by lazy {
        CodesInteractor()
    }

    private val codeTemplatesInteractor by lazy {
        CodeTemplatesInteractor()
    }

    private val customerInteractor by lazy {
        CustomerInteractor()
    }

    fun configureSdk(config: PromofireConfig, deviceSpecsProvider: DeviceSpecsProvider, callback: ErrorCallback) {
        require(configurationJob == null) { "Promofire is already configured" }
        configurationJob = promofireScope.launch {
            val configurationResult = promofireConfigurator.configureSdk(config, deviceSpecsProvider)
            when (configurationResult) {
                is PromofireResult.Success -> {
                    Promofire.isConfigured = true
                }
                is PromofireResult.Error -> {
                    Promofire.isConfigured = false
                    callback.onResult(configurationResult.error)
                    Logger.e("Error during SDK configuration", configurationResult.error)
                }
            }
            configurationJob = null
            isCodeGenerationAvailable { /* Ignore */ }
        }
    }

    fun isCodeGenerationAvailable(callback: ResultCallback<Boolean>) {
        if (isCodeGenerationAvailable) {
            callback.onResult(PromofireResult.Success(true))
            return
        }
        promofireScope.launch {
            waitForConfiguration()
            val isCodeGenerationAvailableResult = codeGenerationInteractor.isCodeGenerationAvailable()
            if (isCodeGenerationAvailableResult is PromofireResult.Success) {
                isCodeGenerationAvailable = isCodeGenerationAvailableResult.value
            }
            callback.onResult(isCodeGenerationAvailableResult)
        }
    }

    fun getCurrentUserCodes(limit: Int, offset: Int, callback: ResultCallback<Codes>) {
        promofireScope.launch {
            waitForConfiguration()
            val currentUserCodesResult = codesInteractor.getCurrentUserCodes(limit, offset)
            callback.onResult(currentUserCodesResult)
        }
    }

    fun getCurrentUserRedeems(
        limit: Int,
        offset: Int,
        from: Date,
        to: Date,
        codeValue: String?,
        callback: ResultCallback<CodeRedeems>,
    ) {
        promofireScope.launch {
            waitForConfiguration()
            val currentUserRedeemsResult = codesInteractor.getCurrentUserRedeems(limit, offset, from, to, codeValue)
            callback.onResult(currentUserRedeemsResult)
        }
    }

    fun getCampaigns(limit: Int, offset: Int, callback: ResultCallback<CodeTemplates>) {
        promofireScope.launch {
            waitForConfiguration()
            val codeTemplatesResult = codeTemplatesInteractor.getCampaigns(limit, offset)
            callback.onResult(codeTemplatesResult)
        }
    }

    fun getCampaignBy(id: String, callback: ResultCallback<CodeTemplate>) {
        promofireScope.launch {
            waitForConfiguration()
            val codeTemplateResult = codeTemplatesInteractor.getCampaignBy(id)
            callback.onResult(codeTemplateResult)
        }
    }

    fun generateCode(params: GenerateCodeParams, callback: ResultCallback<Code>) {
        promofireScope.launch {
            waitForConfiguration()
            val generateCodeResult = codesInteractor.generateCode(params)
            callback.onResult(generateCodeResult)
        }
    }

    fun generateCodes(params: GenerateCodesParams, callback: ResultCallback<List<Code>>) {
        promofireScope.launch {
            waitForConfiguration()
            val generateCodesResult = codesInteractor.generateCodes(params)
            callback.onResult(generateCodesResult)
        }
    }

    fun redeemCode(codeValue: String, callback: ErrorCallback) {
        promofireScope.launch {
            waitForConfiguration()
            val redeemCodeResult = codesInteractor.redeemCode(codeValue)
            if (redeemCodeResult is PromofireResult.Error) {
                callback.onResult(redeemCodeResult.error)
            }
        }
    }

    fun getCurrentUser(callback: ResultCallback<Customer>) {
        promofireScope.launch {
            waitForConfiguration()
            val currentUserResult = customerInteractor.getCurrentUser()
            callback.onResult(currentUserResult)
        }
    }

    fun updateCurrentUser(params: UpdateCustomerParams, callback: ResultCallback<Customer>) {
        promofireScope.launch {
            waitForConfiguration()
            val updateCurrentUserResult = customerInteractor.updateCurrentUser(params)
            callback.onResult(updateCurrentUserResult)
        }
    }

    fun getCodeRedeems(
        limit: Int,
        offset: Int,
        from: Date,
        to: Date,
        codeValue: String?,
        redeemerId: String?,
        callback: ResultCallback<CodeRedeems>,
    ) {
        promofireScope.launch {
            waitForConfiguration()
            val codeRedeemsResult = codesInteractor.getCodeRedeems(limit, offset, from, to, codeValue, redeemerId)
            callback.onResult(codeRedeemsResult)
        }
    }

    private suspend fun waitForConfiguration() {
        if (!Promofire.isConfigured) configurationJob?.join()
    }
}
