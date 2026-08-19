package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.KeyValueStore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfileImportCleanupTest {
    @Test
    fun importingSmallerProfileSetRemovesDiscardedNameKeys() {
        val targetStore = ImportCleanupStore()
        val target = ArenaRepository(targetStore)
        val created = assertNotNull(target.createProfile("Discard After Import"))
        val discardedId = created.activeProfileId
        val discardedKey = "profile_name_v1:$discardedId"
        assertTrue(targetStore.containsKey(discardedKey))

        val source = ArenaRepository(ImportCleanupStore())
        val backup = source.exportBackup()
        assertTrue(target.importBackup(backup))

        assertFalse(targetStore.containsKey(discardedKey))
    }
}

private class ImportCleanupStore : KeyValueStore {
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
