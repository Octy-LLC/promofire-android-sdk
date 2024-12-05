package io.promofire.data.network.core

import io.promofire.utils.PromofireResult
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal typealias EmptyNetworkResult = NetworkResult<Unit>

internal sealed class NetworkResult<out T> {

    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>()

    data class Error(
        val throwable: Throwable,
    ) : NetworkResult<Nothing>()
}

@OptIn(ExperimentalContracts::class)
internal fun <T> NetworkResult<T>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is NetworkResult.Success)
        returns(false) implies (this@isSuccess is NetworkResult.Error)
    }
    return this is NetworkResult.Success
}

internal inline fun <T, R> NetworkResult<T>.mapToPromofireResult(
    transform: ((T) -> R),
): PromofireResult<R> = when (this) {
    is NetworkResult.Success -> PromofireResult.Success(transform(data))
    is NetworkResult.Error -> PromofireResult.Error(throwable)
}

internal fun <T> NetworkResult<T>.mapToPromofireResult(): PromofireResult<T> = mapToPromofireResult { it }
