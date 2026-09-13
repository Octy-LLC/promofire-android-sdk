package io.promofire.models

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build

/**
 * Данные о приложении, которые SDK не может определить сам.
 *
 * Модель устройства, версию ОС и версию SDK библиотека подставляет
 * самостоятельно — передавать их не нужно.
 */
public data class DeviceInfo(
    /** Номер сборки приложения. */
    val appBuild: String,
    /** Версия приложения. */
    val appVersion: String,
) {

    public companion object {

        /**
         * Достаёт версию и номер сборки из манифеста приложения.
         *
         * Удобная замена ручному заполнению, но не обязательная: значения
         * можно передать и напрямую, например из полей BuildConfig.
         */
        public fun from(context: Context): DeviceInfo {
            val info = runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }.getOrNull()

            return DeviceInfo(
                appBuild = info?.buildNumber ?: UNKNOWN,
                appVersion = info?.versionName ?: UNKNOWN,
            )
        }

        private const val UNKNOWN = "Unknown"

        private val PackageInfo.buildNumber: String
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                versionCode.toString()
            }
    }
}
