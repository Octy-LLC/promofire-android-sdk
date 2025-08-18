package io.promofire.data

internal interface Storage {

    fun getString(key: String): String?

    suspend fun saveString(key: String, value: String?)

    suspend fun remove(key: String)

    suspend fun clear()
}
