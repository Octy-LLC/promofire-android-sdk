package io.promofire.utils

import java.util.Date

internal val Date.timeInSeconds: Long
    get() = time / 1000L
