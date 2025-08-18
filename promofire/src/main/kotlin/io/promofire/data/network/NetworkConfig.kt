package io.promofire.data.network

import kotlinx.serialization.json.Json

internal object NetworkConfig {

    const val BASE_URL = "https://api.promofire.io/"

    val Json = Json {
        ignoreUnknownKeys = true
    }
}
