package io.promofire.data.network.api

import io.promofire.data.network.api.auth.AuthApi
import io.promofire.data.network.api.auth.AuthApiImpl
import io.promofire.data.network.api.code_templates.CodeTemplatesApi
import io.promofire.data.network.api.code_templates.CodeTemplatesApiImpl
import io.promofire.data.network.api.codes.CodesApi
import io.promofire.data.network.api.codes.CodesApiImpl
import io.promofire.data.network.api.customers.CustomersApi
import io.promofire.data.network.api.customers.CustomersApiImpl
import io.promofire.data.network.client

internal val authApi: AuthApi by lazy { AuthApiImpl(client) }

internal val codeTemplatesApi: CodeTemplatesApi by lazy { CodeTemplatesApiImpl(client) }

internal val codesApi: CodesApi by lazy { CodesApiImpl(client) }

internal val customersApi: CustomersApi by lazy { CustomersApiImpl(client) }
