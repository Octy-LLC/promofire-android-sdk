package io.promofire.data.local

import android.content.Context
import io.promofire.data.Storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class PreferencesStorage(
    appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Storage {

    companion object {
        private const val PREFS_NAME = "promofire_storage"
    }

    private val preferences by lazy {
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val editor by lazy {
        preferences.edit()
    }

    override fun getString(key: String): String? = preferences.getString(key, null)

    override suspend fun saveString(key: String, value: String?): Unit = withContext(ioDispatcher) {
        editor.putString(key, value).commit()
    }

    override suspend fun clear(): Unit = withContext(ioDispatcher) {
        editor.clear().commit()
    }
}
