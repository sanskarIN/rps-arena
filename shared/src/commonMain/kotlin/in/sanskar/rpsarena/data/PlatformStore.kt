package `in`.sanskar.rpsarena.data

interface KeyValueStore {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)

    /**
     * Remove a persisted key. The default keeps lightweight test/custom stores source-compatible;
     * production platform stores override this with their native deletion primitive.
     */
    fun remove(key: String) {
        putString(key, "")
    }
}

expect object PlatformStore {
    fun initialize(platformContext: Any? = null)
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
    fun remove(key: String)
}

internal object DefaultKeyValueStore : KeyValueStore {
    override fun getString(key: String, defaultValue: String): String =
        PlatformStore.getString(key, defaultValue)

    override fun putString(key: String, value: String) {
        PlatformStore.putString(key, value)
    }

    override fun remove(key: String) {
        PlatformStore.remove(key)
    }
}
