package `in`.sanskar.rpsarena.data

import platform.Foundation.NSUserDefaults

actual object PlatformStore {
    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    actual fun initialize(platformContext: Any?) = Unit

    actual fun getString(key: String, defaultValue: String): String =
        defaults.stringForKey(key) ?: defaultValue

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}
