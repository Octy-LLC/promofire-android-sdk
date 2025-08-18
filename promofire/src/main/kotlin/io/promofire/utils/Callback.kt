package io.promofire.utils

public fun interface Callback<T> {
    public fun onResult(result: T)
}

public fun interface ResultCallback<T> : Callback<PromofireResult<T>>

public fun interface EmptyResultCallback : ResultCallback<Unit>
