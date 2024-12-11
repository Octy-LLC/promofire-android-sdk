package io.promofire.data

internal object TokensStorage {

    private const val KEY_ACCESS_TOKEN = "key_access_token"

    private var storage: Storage? = null

    val accessToken: String?
        get() = storage?.getString(KEY_ACCESS_TOKEN)

    fun init(storage: Storage) {
        this.storage = storage
    }

    suspend fun saveAccessToken(accessToken: String) {
        storage?.saveString(KEY_ACCESS_TOKEN, accessToken)
    }
}
