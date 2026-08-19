package in.sanskar.rpsarena.data

interface KeyValueStore {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
}

expect object PlatformStore : KeyValueStore {
    fun initialize(platformContext: Any? = null)
    override fun getString(key: String, defaultValue: String): String
    override fun putString(key: String, value: String)
}
