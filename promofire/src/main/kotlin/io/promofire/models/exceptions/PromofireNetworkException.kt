package io.promofire.models.exceptions

public class PromofireNetworkException(
    override val message: String?,
    public val error: String?,
    public val statusCode: Int,
) : Exception()
