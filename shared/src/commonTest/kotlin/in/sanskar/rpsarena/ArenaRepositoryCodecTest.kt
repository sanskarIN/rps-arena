package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.data.KeyValueStore
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.LocalProfilesState
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArenaRepositoryCodecTest {
    private val repository = ArenaRepository(MemoryStore())

    @Test
    fun settingsRoundTrip() {
        val value = ArenaSettings(
            darkTheme = true,
            followSystemTheme = false,
            reducedMotion = true,
            onboardingComplete = true,
        )
        assertEquals(value, repository.decodeSettings(repository.encodeSettings(value)))
    }

    @Test
    fun legacySettingsDecodeWithoutPhantomFlags() {
        val legacy = "true|false|true|false|true|true|true"
        assertEquals(
            ArenaSettings(
                darkTheme = true,
                followSystemTheme = false,
                reducedMotion = true,
                onboardingComplete = true,
            ),
            repository.decodeSettings(legacy),
        )
    }

    @Test
    fun statsRoundTrip() {
        val value = ArenaStats(12, 7, 3, 2, 4, 2)
        assertEquals(value, repository.decodeStats(repository.encodeStats(value)))
    }

    @Test
    fun configRoundTrip() {
        val value = MatchConfig(
            variant = GameVariant.LIZARD_SPOCK,
            opponentMode = OpponentMode.LOCAL_TWO_PLAYER,
            difficulty = Difficulty.EXPERT,
            matchMode = MatchMode.TOURNAMENT,
            seed = 42,
            roundTimerSeconds = 25,
        )
        assertEquals(value, repository.decodeConfig(repository.encodeConfig(value)))
    }

    @Test
    fun localProfilesCanBeCreatedRenamedActivatedAndDeleted() {
        val store = MemoryStore()
        val source = ArenaRepository(store)

        assertEquals(LocalProfilesState.DEFAULT_LOCAL_PROFILE, source.loadProfilesState().activeProfile)
        val second = assertNotNull(source.createProfile("  Challenger   Two  "))
        assertEquals(2, second.profiles.size)
        assertEquals("Challenger Two", second.activeProfile.displayName)

        val renamed = assertNotNull(source.renameProfile(second.activeProfileId, "Arena Ace"))
        assertEquals("Arena Ace", renamed.activeProfile.displayName)

        val firstId = renamed.profiles.first().id
        val activated = assertNotNull(source.activateProfile(firstId))
        assertEquals(firstId, activated.activeProfileId)

        val deleted = assertNotNull(source.deleteProfile(firstId))
        assertEquals(1, deleted.profiles.size)
        assertEquals("Arena Ace", deleted.activeProfile.displayName)
        assertEquals(deleted, ArenaRepository(store).loadProfilesState())
    }

    @Test
    fun invalidOrExcessiveProfilesAreRejected() {
        val source = ArenaRepository(MemoryStore())
        assertEquals(null, source.createProfile("   "))
        assertEquals(null, source.createProfile("x".repeat(ArenaRepository.MAX_PROFILE_NAME_LENGTH + 1)))
        assertEquals(null, source.createProfile("bad\nname"))

        repeat(ArenaRepository.MAX_PROFILES - 1) { index ->
            assertNotNull(source.createProfile("Player ${index + 2}"))
        }
        assertEquals(ArenaRepository.MAX_PROFILES, source.loadProfilesState().profiles.size)
        assertEquals(null, source.createProfile("One too many"))
    }

    @Test
    fun backupRestoresAllPersistedDataIncludingProfiles() {
        val source = ArenaRepository(MemoryStore())
        val settings = ArenaSettings(
            darkTheme = true,
            followSystemTheme = false,
            reducedMotion = true,
            onboardingComplete = true,
        )
        val stats = ArenaStats(10, 6, 3, 1, 4, 2)
        val config = MatchConfig(
            variant = GameVariant.LIZARD_SPOCK,
            difficulty = Difficulty.EXPERT,
            matchMode = MatchMode.ENDLESS,
            seed = 901,
            roundTimerSeconds = 15,
        )
        source.saveSettings(settings)
        source.saveStats(stats)
        source.saveConfig(config)
        source.addHistory("Rock vs Scissors — Player 1 won")
        source.addHistory("Paper vs Paper — Draw")
        source.renameProfile(source.loadProfilesState().activeProfileId, "Primary Player")
        source.createProfile("Second Player")

        val target = ArenaRepository(MemoryStore())
        assertTrue(target.importBackup(source.exportBackup()))
        assertEquals(settings, target.loadSettings())
        assertEquals(stats, target.loadStats())
        assertEquals(config, target.loadConfig())
        assertEquals(source.loadHistory(), target.loadHistory())
        assertEquals(source.loadProfilesState(), target.loadProfilesState())
    }

    @Test
    fun legacyV1BackupMigratesToDefaultLocalProfile() {
        val target = ArenaRepository(MemoryStore())
        val legacy = """
            RPS_ARENA_BACKUP_V1
            settings=false|true|false|true
            stats=0|0|0|0|0|0
            config=CLASSIC|CPU|NORMAL|BEST_OF_3|20260819|0
            history=
        """.trimIndent()

        assertTrue(target.importBackup(legacy))
        assertEquals(LocalProfilesState.default(), target.loadProfilesState())
    }

    @Test
    fun malformedBackupIsRejectedWithoutMutation() {
        val target = ArenaRepository(MemoryStore())
        val original = ArenaStats(1, 1, 0, 0, 1, 1)
        target.saveStats(original)

        assertFalse(target.importBackup("not-a-rps-arena-backup"))
        assertEquals(original, target.loadStats())
    }

    @Test
    fun corruptedStoredStatsFallBackToSafeDefaults() {
        val store = MemoryStore().apply {
            putString("stats_v1", "10|9|9|9|0|20")
        }
        assertEquals(ArenaStats(), ArenaRepository(store).loadStats())
    }
}

private class MemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun getString(key: String, defaultValue: String): String = values[key] ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }
}
