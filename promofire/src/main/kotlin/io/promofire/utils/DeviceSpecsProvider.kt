package io.promofire.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

internal interface DeviceSpecsProvider {

    val deviceName: String

    val osVersion: String

    val appVersionAndCode: Pair<String, String>
}

internal class AndroidDeviceSpecsProvider(
    private val appContext: Context,
) : DeviceSpecsProvider {

    override val deviceName: String
        get() {
            val manufacturer: String = Build.MANUFACTURER
            val model: String = Build.MODEL
            val hasDeviceManufacturer = model.startsWith(manufacturer, ignoreCase = true)
            return if (hasDeviceManufacturer) {
                model
            } else {
                "$manufacturer $model"
            }
        }

    override val osVersion: String
        get() = "Android ${Build.VERSION.RELEASE}"

    override val appVersionAndCode: Pair<String, String>
        get() = try {
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            val version = packageInfo.versionName ?: "Unknown"
            val versionCode = packageInfo.stringVersionCode
            version to versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown" to "Unknown"
        }

    private val PackageInfo.stringVersionCode: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            versionCode.toString()
        }
}
