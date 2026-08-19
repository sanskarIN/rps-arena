package in.sanskar.rpsarena.data

import android.content.Context
import android.content.SharedPreferences

actual object PlatformStore : KeyValueStore {
    private var preferences: SharedPreferences? = null

    actual fun initialize(platformContext: Any?) {
        val context = platformContext as? Context
            ?: error("Android PlatformStore requires a Context")
        preferences = context.applicationContext.getSharedPreferences("rps_arena", Context.MODE_PRIVATE)
    }

    actual override fun getString(key: String, defaultValue: String): String =
        preferences?.getString(key, defaultValue) ?: defaultValue

    actual override fun putString(key: String, value: String) {
        preferences?.edit()?.putString(key, value)?.apply()
    }
}
