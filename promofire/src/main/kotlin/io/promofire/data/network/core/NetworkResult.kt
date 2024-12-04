package io.promofire.data.network.core

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
