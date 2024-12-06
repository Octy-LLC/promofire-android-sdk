package io.promofire.interactors

import io.promofire.data.mappers.toDto
import io.promofire.data.mappers.toModel
import io.promofire.data.network.api.customersApi
import io.promofire.data.network.core.mapToPromofireResult
import io.promofire.models.Customer
import io.promofire.models.params.UpdateCustomerParams
import io.promofire.utils.PromofireResult

internal class CustomerInteractor {

    suspend fun getCurrentUser(): PromofireResult<Customer> {
        return customersApi.getMyCustomer().mapToPromofireResult { it.toModel() }
    }

    suspend fun updateCurrentUser(params: UpdateCustomerParams): PromofireResult<Customer> {
        return customersApi.updateMyCustomer(params.toDto()).mapToPromofireResult { it.toModel() }
    }
}
