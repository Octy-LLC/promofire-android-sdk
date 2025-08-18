package io.promofire.utils

public sealed class PromofireResult<out T> {

    @ConsistentCopyVisibility
    public data class Success<T> internal constructor(
        val value: T,
    ) : PromofireResult<T>()

    @ConsistentCopyVisibility
    public data class Error internal constructor(
        val error: Throwable,
    ) : PromofireResult<Nothing>()
}
