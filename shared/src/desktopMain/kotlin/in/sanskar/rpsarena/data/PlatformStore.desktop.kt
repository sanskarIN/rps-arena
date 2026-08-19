package `in`.sanskar.rpsarena.data

import java.util.prefs.Preferences

actual object PlatformStore {
    private val preferences: Preferences by lazy {
        Preferences.userRoot().node("in/sanskar/rpsarena")
    }

    actual fun initialize(platformContext: Any?) = Unit

    actual fun getString(key: String, defaultValue: String): String =
        preferences.get(key, defaultValue)

    actual fun putString(key: String, value: String) {
        preferences.put(key, value)
    }
}
