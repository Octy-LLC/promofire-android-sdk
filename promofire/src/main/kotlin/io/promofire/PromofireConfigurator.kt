package io.promofire

import io.promofire.config.PromofireConfig
import io.promofire.data.TokensStorage
import io.promofire.data.network.api.auth.requests.SdkAuthRequestDto
import io.promofire.data.network.api.authApi
import io.promofire.data.network.api.customers.models.CreateCustomerRequestDto
import io.promofire.data.network.api.customers.models.CreatePresetRequestDto
import io.promofire.data.network.api.customersApi
import io.promofire.data.network.core.isSuccess
import io.promofire.logger.Logger
import io.promofire.logger.e
import io.promofire.models.UserInfo
import io.promofire.utils.DeviceSpecsProvider

internal class PromofireConfigurator {

    companion object {
        private const val PLATFORM_ANDROID = "ANDROID"
    }

    suspend fun configureSdk(config: PromofireConfig, deviceSpecsProvider: DeviceSpecsProvider) {
        val sdkAuthRequest = SdkAuthRequestDto(config.projectName, config.secret)
        val authResult = authApi.signInWithSdk(sdkAuthRequest)
        if (!authResult.isSuccess()) {
            Logger.e("Error during SDK configuration", authResult.throwable)
            return
        }
        TokensStorage.saveAccessToken(authResult.data.accessToken)

        val createPresetRequest = CreatePresetRequestDto(PLATFORM_ANDROID)
        val customersResult = customersApi.createCustomerPreset(createPresetRequest)
        if (!customersResult.isSuccess()) {
            Logger.e("Error during SDK configuration", customersResult.throwable)
            return
        }
        TokensStorage.saveAccessToken(customersResult.data.accessToken)

        val createCustomerRequest = createCustomerRequest(config.userInfo, deviceSpecsProvider)
        val createCustomerResult = customersApi.upsertCustomer(createCustomerRequest)
        if (!createCustomerResult.isSuccess()) {
            Logger.e("Error during SDK configuration", createCustomerResult.throwable)
            return
        }
        TokensStorage.saveAccessToken(createCustomerResult.data.accessToken)

        Promofire.isConfigured = true
    }

    private fun createCustomerRequest(
        userInfo: UserInfo?,
        deviceSpecsProvider: DeviceSpecsProvider,
    ): CreateCustomerRequestDto {
        val deviceName = deviceSpecsProvider.deviceName
        val (appVersion, appBuild) = deviceSpecsProvider.appVersionAndCode
        return CreateCustomerRequestDto(
            platform = PLATFORM_ANDROID,
            device = deviceName,
            os = deviceSpecsProvider.osVersion,
            appBuild = appBuild,
            appVersion = appVersion,
            sdkVersion = BuildConfig.VERSION_NAME,
            tenantAssignedId = userInfo?.tenantId,
            firstName = userInfo?.firstName,
            lastName = userInfo?.lastName,
            email = userInfo?.email,
            phone = userInfo?.phone,
        )
    }
}
