package io.promofire

import io.promofire.config.PromofireConfig
import io.promofire.data.Storage
import io.promofire.data.TokensStorage
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
import io.promofire.models.params.UpdateCodeParams
import io.promofire.models.params.UpdateCustomerParams
import io.promofire.utils.DeviceSpecsProvider
import io.promofire.utils.EmptyResultCallback
import io.promofire.utils.PromofireResult
import io.promofire.utils.ResultCallback
import io.promofire.utils.promofireScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

internal class PromofireImpl(
    storage: Storage,
    private val deviceSpecsProvider: DeviceSpecsProvider,
) {

    private val promofireConfigurator = PromofireConfigurator()

    private var isCodeGenerationAvailable = AtomicBoolean(false)

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

    init {
        TokensStorage.init(storage)
    }

    fun configureSdk(
        config: PromofireConfig,
        callback: EmptyResultCallback,
    ) {
        promofireScope.launch {
            val configurationResult = promofireConfigurator.configureSdk(config, deviceSpecsProvider)
            when (configurationResult) {
                is PromofireResult.Success -> {
                    callback.onResult(configurationResult)
                    isCodeGenerationAvailable { /* Ignore */ }
                }
                is PromofireResult.Error -> {
                    callback.onResult(configurationResult)
                    Logger.e("Error during SDK configuration", configurationResult.error)
                }
            }
        }
    }

    fun isCodeGenerationAvailable(callback: ResultCallback<Boolean>) {
        if (isCodeGenerationAvailable.get()) {
            callback.onResult(PromofireResult.Success(true))
            return
        }
        promofireScope.launch {
            waitForConfiguration()
            val isCodeGenerationAvailableResult = codeGenerationInteractor.isCodeGenerationAvailable()
            if (isCodeGenerationAvailableResult is PromofireResult.Success) {
                isCodeGenerationAvailable.set(isCodeGenerationAvailableResult.value)
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

    fun getCodeByValue(codeValue: String, callback: ResultCallback<Code>) {
        promofireScope.launch {
            waitForConfiguration()
            val codeResult = codesInteractor.getCodeByValue(codeValue)
            callback.onResult(codeResult)
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

    fun updateCode(codeValue: String, params: UpdateCodeParams, callback: ResultCallback<Code>) {
        promofireScope.launch {
            waitForConfiguration()
            val updateCodeResult = codesInteractor.updateCode(codeValue, params)
            callback.onResult(updateCodeResult)
        }
    }

    fun redeemCode(codeValue: String, callback: EmptyResultCallback) {
        promofireScope.launch {
            waitForConfiguration()
            val redeemCodeResult = codesInteractor.redeemCode(codeValue)
            callback.onResult(redeemCodeResult)
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

    fun logout(callback: EmptyResultCallback) {
        promofireScope.launch {
            promofireConfigurator.configurationJob()?.cancelAndJoin()
            TokensStorage.clear()
            isCodeGenerationAvailable.set(false)
            callback.onResult(PromofireResult.Success(Unit))
        }
    }

    private suspend fun waitForConfiguration() {
        promofireConfigurator.configurationJob()?.join()
    }
}
