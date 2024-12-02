package io.promofire.data.network.api

import io.promofire.data.network.api.auth.AuthApi
import io.promofire.data.network.api.auth.AuthApiImpl
import io.promofire.data.network.client

internal val authApi: AuthApi by lazy { AuthApiImpl(client) }
