package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.KeyValueStore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfilePersistenceCleanupTest {
    @Test
    fun fullResetRemovesPersistedNamesForDiscardedProfiles() {
        val store = CleanupMemoryStore()
        val repository = ArenaRepository(store)
        val created = assertNotNull(repository.createProfile("Reset Me"))
        val discardedId = created.activeProfileId
        val discardedKey = "profile_name_v1:$discardedId"
        assertTrue(store.containsKey(discardedKey))

        repository.resetAll()

        assertFalse(store.containsKey(discardedKey))
    }
}

private class CleanupMemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String, defaultValue: String): String = values[key] ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    fun containsKey(key: String): Boolean = key in values
}
