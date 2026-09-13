package io.promofire.utils

import android.os.Build

/**
 * Модель устройства и версия ОС. Контекст приложения для этого не нужен —
 * всё берётся из [Build].
 */
internal class AndroidDeviceSpecsProvider {

    val deviceName: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL

            return if (model.startsWith(manufacturer, ignoreCase = true)) {
                model
            } else {
                "$manufacturer $model"
            }
        }

    val osVersion: String
        get() = "Android ${Build.VERSION.RELEASE}"
}
