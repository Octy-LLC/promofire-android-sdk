package io.promofire.utils

import android.os.Build

/**
 * Модель устройства и версия ОС. Контекст приложения для этого не нужен —
 * всё берётся из [Build].
 *
 * Поля [Build] объявлены ненулевыми, но на деле пусты в JVM-тестах, где
 * фреймворк возвращает значения по умолчанию, и известны случаи пустых
 * значений на перепрошитых устройствах. Бэкенд требует `device` и `os`
 * непустыми строками, поэтому здесь ни одно из них не может выродиться
 * ни в `null`, ни в пустую строку.
 */
internal class AndroidDeviceSpecsProvider {

    val deviceName: String
        get() {
            val manufacturer = Build.MANUFACTURER.orUnknown()
            val model = Build.MODEL.orUnknown()

            return if (model.startsWith(manufacturer, ignoreCase = true)) {
                model
            } else {
                "$manufacturer $model"
            }
        }

    val osVersion: String
        get() = "Android ${Build.VERSION.RELEASE.orUnknown()}"

    private fun String?.orUnknown(): String =
        if (isNullOrBlank()) UNKNOWN else this

    private companion object {
        const val UNKNOWN = "unknown"
    }
}
