package io.promofire.resources

import io.ktor.http.HttpMethod
import io.promofire.internal.ApiClient
import io.promofire.internal.CustomerDto
import io.promofire.internal.PromofireJson
import io.promofire.internal.UpdateCustomerRequestDto
import io.promofire.internal.toModel
import io.promofire.models.Customer
import io.promofire.models.params.UpdateCustomerParams

public class CustomerResource internal constructor(
    private val api: ApiClient,
) {

    public suspend fun getProfile(): Customer {
        val raw = api.request(HttpMethod.Get, "/customers/me")

        return PromofireJson.decodeFromString<CustomerDto>(raw).toModel()
    }

    /** Отправляет только те поля, которые заданы явно. */
    public suspend fun updateProfile(params: UpdateCustomerParams): Customer {
        val raw = api.request(
            method = HttpMethod.Patch,
            path = "/customers/me",
            body = UpdateCustomerRequestDto(
                firstName = params.firstName,
                lastName = params.lastName,
                email = params.email,
                phone = params.phone,
            ),
        )

        return PromofireJson.decodeFromString<CustomerDto>(raw).toModel()
    }
}
