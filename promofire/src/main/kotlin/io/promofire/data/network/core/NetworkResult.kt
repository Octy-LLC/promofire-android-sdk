package io.promofire.data.network.core

internal typealias EmptyNetworkResult = NetworkResult<Unit>

internal sealed class NetworkResult<out T> {

    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>()

    data class Error(
        val throwable: Throwable,
    ) : NetworkResult<Nothing>()
}
