package in.sanskar.rpsarena.data

import java.util.prefs.Preferences

actual object PlatformStore : KeyValueStore {
    private val preferences: Preferences by lazy {
        Preferences.userRoot().node("in/sanskar/rpsarena")
    }

    actual fun initialize(platformContext: Any?) = Unit

    actual override fun getString(key: String, defaultValue: String): String =
        preferences.get(key, defaultValue)

    actual override fun putString(key: String, value: String) {
        preferences.put(key, value)
    }
}
