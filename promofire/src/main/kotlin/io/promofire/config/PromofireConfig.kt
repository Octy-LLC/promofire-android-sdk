package io.promofire.config

import io.promofire.models.UserInfo

public class PromofireConfig internal constructor(
    internal val secret: String,
    internal val userInfo: UserInfo?,
) {

    public class Builder(
        private val secret: String,
    ) {

        private var userInfo: UserInfo? = null

        public fun setUserInfo(userInfo: UserInfo?): Builder = apply {
            this.userInfo = userInfo
        }

        public fun build(): PromofireConfig = PromofireConfig(
            secret = secret,
            userInfo = userInfo,
        )
    }
}
