package io.promofire

import io.promofire.config.PromofireConfig
import io.promofire.data.TokensStorage
import io.promofire.data.network.api.auth.requests.SdkCustomerAuthRequestDto
import io.promofire.data.network.api.authApi
import io.promofire.data.network.core.isSuccess
import io.promofire.data.network.core.mapToPromofireResult
import io.promofire.models.Platform
import io.promofire.models.exceptions.PromofireConfigurationCancelException
import io.promofire.utils.DeviceSpecsProvider
import io.promofire.utils.PromofireResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PromofireConfigurator {

    private val mutex = Mutex()

    private var configurationRequest: Deferred<PromofireResult<Unit>>? = null

    suspend fun configurationJob(): Job? = mutex.withLock { configurationRequest }

    suspend fun configureSdk(
        config: PromofireConfig,
        deviceSpecsProvider: DeviceSpecsProvider,
    ): PromofireResult<Unit> = coroutineScope {
        val configurationDeferred = mutex.withLock {
            configurationRequest ?: configureSdkAsync(config, deviceSpecsProvider).also { configurationRequest = it }
        }
        try {
            configurationDeferred.await()
        } catch (e: CancellationException) {
            TokensStorage.clear()
            PromofireResult.Error(PromofireConfigurationCancelException())
        } finally {
            mutex.withLock { configurationRequest = null }
        }
    }

    private fun CoroutineScope.configureSdkAsync(
        config: PromofireConfig,
        deviceSpecsProvider: DeviceSpecsProvider,
    ): Deferred<PromofireResult<Unit>> = async { configurePromofireSdk(config, deviceSpecsProvider) }

    private suspend fun configurePromofireSdk(
        config: PromofireConfig,
        deviceSpecsProvider: DeviceSpecsProvider,
    ): PromofireResult<Unit> {
        if (TokensStorage.accessToken != null) {
            return PromofireResult.Success(Unit)
        }

        val sdkCustomerAuthRequest = createSdkCustomerAuthRequest(config, deviceSpecsProvider)
        val authResult = authApi.signInWithSdk(sdkCustomerAuthRequest)
        if (!authResult.isSuccess()) {
            TokensStorage.clear()
            return authResult.mapToPromofireResult()
        }
        TokensStorage.saveAccessToken(authResult.data.accessToken)

        return PromofireResult.Success(Unit)
    }

    private fun createSdkCustomerAuthRequest(
        config: PromofireConfig,
        deviceSpecsProvider: DeviceSpecsProvider,
    ): SdkCustomerAuthRequestDto {
        val userInfo = config.userInfo
        val deviceName = deviceSpecsProvider.deviceName
        val (appVersion, appBuild) = deviceSpecsProvider.appVersionAndCode
        return SdkCustomerAuthRequestDto(
            platform = Platform.ANDROID,
            device = deviceName,
            os = deviceSpecsProvider.osVersion,
            appBuild = appBuild,
            appVersion = appVersion,
            sdkVersion = BuildConfig.VERSION_NAME,
            customerUserId = userInfo?.customerUserId,
            firstName = userInfo?.firstName,
            lastName = userInfo?.lastName,
            email = userInfo?.email,
            phone = userInfo?.phone,
            secret = config.secret,
        )
    }
}
