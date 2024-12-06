package io.promofire

import io.promofire.config.PromofireConfig
import io.promofire.interactors.CodeGenerationInteractor
import io.promofire.interactors.CodeTemplatesInteractor
import io.promofire.interactors.CodesInteractor
import io.promofire.models.CodeTemplates
import io.promofire.models.Codes
import io.promofire.utils.DeviceSpecsProvider
import io.promofire.utils.PromofireResult
import io.promofire.utils.ResultCallback
import io.promofire.utils.promofireScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    fun configureSdk(config: PromofireConfig, deviceSpecsProvider: DeviceSpecsProvider) {
        require(configurationJob == null) { "Promofire is already configured" }
        configurationJob = promofireScope.launch {
            promofireConfigurator.configureSdk(config, deviceSpecsProvider)
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

    fun getCampaigns(limit: Int, offset: Int, callback: ResultCallback<CodeTemplates>) {
        promofireScope.launch {
            waitForConfiguration()
            val codeTemplatesResult = codeTemplatesInteractor.getCampaigns(limit, offset)
            callback.onResult(codeTemplatesResult)
        }
    }

    private suspend fun waitForConfiguration() {
        if (!Promofire.isConfigured) configurationJob?.join()
    }
}
