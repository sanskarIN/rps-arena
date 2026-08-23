package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaStore

internal class TestArenaStore(initial: Map<String, String> = emptyMap()) : ArenaStore {
    private val values = initial.toMutableMap()

    override fun getString(key: String, defaultValue: String): String = values[key] ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    operator fun get(key: String): String? = values[key]
}
