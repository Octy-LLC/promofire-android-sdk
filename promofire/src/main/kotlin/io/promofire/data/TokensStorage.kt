package io.promofire.data

internal object TokensStorage {

    var accessToken: String? = null
        private set

    fun saveAccessToken(accessToken: String) {
        TokensStorage.accessToken = accessToken
    }
}
