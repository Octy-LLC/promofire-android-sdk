package io.promofire.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.headers
import io.ktor.serialization.kotlinx.json.json

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
}
