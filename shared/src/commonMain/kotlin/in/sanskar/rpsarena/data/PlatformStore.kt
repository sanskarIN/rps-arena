package `in`.sanskar.rpsarena.data

interface KeyValueStore {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
}

expect object PlatformStore {
    fun initialize(platformContext: Any? = null)
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
}

internal object DefaultKeyValueStore : KeyValueStore {
    override fun getString(key: String, defaultValue: String): String =
        PlatformStore.getString(key, defaultValue)

    override fun putString(key: String, value: String) {
        PlatformStore.putString(key, value)
    }
}
