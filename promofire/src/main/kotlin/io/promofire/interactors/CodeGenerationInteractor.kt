package io.promofire.interactors

import io.promofire.data.network.api.codeTemplatesApi
import io.promofire.data.network.core.mapToPromofireResult
import io.promofire.utils.PromofireResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class CodeGenerationInteractor {

    private val mutex = Mutex()

    private var ongoingRequest: Deferred<PromofireResult<Boolean>>? = null

    suspend fun isCodeGenerationAvailable(): PromofireResult<Boolean> = coroutineScope {
        val isCodeGenerationAvailableDeferred = mutex.withLock {
            ongoingRequest ?: isGenerationAvailableAsync().also { ongoingRequest = it }
        }
        val isCodeGenerationAvailable = isCodeGenerationAvailableDeferred.await()
        mutex.withLock { ongoingRequest = null }

        return@coroutineScope isCodeGenerationAvailable
    }

    private fun CoroutineScope.isGenerationAvailableAsync(): Deferred<PromofireResult<Boolean>> {
        return async { codeTemplatesApi.getCodeTemplates(limit = 1).mapToPromofireResult { it.templates.isNotEmpty() } }
    }
}
