package io.promofire.models.exceptions

public class PromofireConfigurationCancelException : Exception() {

    override val message: String
        get() = "Promofire configuration was canceled"
}
