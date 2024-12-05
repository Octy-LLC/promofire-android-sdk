package io.promofire

import io.promofire.config.PromofireConfig
import io.promofire.utils.DeviceSpecsProvider
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

    fun configureSdk(config: PromofireConfig, deviceSpecsProvider: DeviceSpecsProvider) {
        require(configurationJob == null) { "Promofire is already configured" }
        configurationJob = promofireScope.launch {
            promofireConfigurator.configureSdk(config, deviceSpecsProvider)
            configurationJob = null
        }
    }
    private suspend fun waitForConfiguration() {
        if (!Promofire.isConfigured) configurationJob?.join()
    }
}
