package `in`.sanskar.rpsarena.data

import kotlinx.browser.window

actual object PlatformStore {
    actual fun initialize(platformContext: Any?) = Unit

    actual fun getString(key: String, defaultValue: String): String =
        window.localStorage.getItem(key) ?: defaultValue

    actual fun putString(key: String, value: String) {
        window.localStorage.setItem(key, value)
    }
}
