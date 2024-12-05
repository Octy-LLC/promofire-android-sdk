package io.promofire.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal val promofireScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
