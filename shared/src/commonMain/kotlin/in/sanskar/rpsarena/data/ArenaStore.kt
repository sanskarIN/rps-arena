package `in`.sanskar.rpsarena.data

/** Minimal key-value boundary used by [ArenaRepository]. */
interface ArenaStore {
    fun getString(key: String, defaultValue: String = ""): String
    fun putString(key: String, value: String)
}

/** Production adapter that preserves the existing platform persistence behavior. */
internal object PlatformArenaStore : ArenaStore {
    override fun getString(key: String, defaultValue: String): String =
        PlatformStore.getString(key, defaultValue)

    override fun putString(key: String, value: String) {
        PlatformStore.putString(key, value)
    }
}
