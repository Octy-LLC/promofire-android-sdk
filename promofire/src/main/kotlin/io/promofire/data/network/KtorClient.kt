package io.promofire.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.serialization.kotlinx.json.json
import io.promofire.logger.Logger
import io.promofire.logger.i
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal val client = HttpClient(OkHttp) {
    defaultRequest {
        url(NetworkConfig.BASE_URL)
        headers {
            append("Content-Type", "application/json")
        }
    }
    install(ContentNegotiation) {
        json(NetworkConfig.Json)
    }
    install(Logging) {
        logger = object : KtorLogger {
            override fun log(message: String) {
                Logger.i(message)
            }
        }
        level = LogLevel.ALL
    }
}
