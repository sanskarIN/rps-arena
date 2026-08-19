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
import kotlin.test.assertNull
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
    fun deletingProfileRemovesPersistedDisplayNameKey() {
        val store = MemoryStore()
        val source = ArenaRepository(store)
        val created = assertNotNull(source.createProfile("Private Alias"))
        val profileId = created.activeProfileId
        val profileNameKey = "profile_name_v1:$profileId"
        assertTrue(store.containsKey(profileNameKey))

        assertNotNull(source.deleteProfile(profileId))

        assertFalse(store.containsKey(profileNameKey))
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
    fun historyReplacementRejectsInvalidEntriesWithoutMutation() {
        val source = ArenaRepository(MemoryStore())
        source.addHistory("Rock vs Scissors — Player 1 won")
        val original = source.loadHistory()

        assertFalse(source.replaceHistory(listOf("valid", "")))
        assertEquals(original, source.loadHistory())
        assertFalse(source.replaceHistory(listOf("bad\nline")))
        assertEquals(original, source.loadHistory())
    }

    @Test
    fun backupPreviewDoesNotMutateTarget() {
        val source = ArenaRepository(MemoryStore())
        source.saveStats(ArenaStats(3, 2, 1, 0, 2, 0))
        source.renameProfile(source.loadProfilesState().activeProfileId, "Preview Player")
        source.addHistory("Rock vs Scissors — Preview Player won")

        val target = ArenaRepository(MemoryStore())
        val original = ArenaStats(1, 0, 0, 1, 0, 0)
        target.saveStats(original)
        val preview = assertNotNull(target.previewBackup(source.exportBackup()))

        assertEquals(2, preview.formatVersion)
        assertEquals("Preview Player", preview.activeProfileName)
        assertEquals(listOf("Preview Player"), preview.profileNames)
        assertEquals(3, preview.stats.roundsPlayed)
        assertEquals(1, preview.historyEntries)
        assertEquals(original, target.loadStats())
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

        val preview = assertNotNull(target.previewBackup(legacy))
        assertEquals(1, preview.formatVersion)
        assertTrue(target.importBackup(legacy))
        assertEquals(LocalProfilesState.default(), target.loadProfilesState())
    }

    @Test
    fun invalidBackupHistoryIsRejectedBeforeAnyMutation() {
        val target = ArenaRepository(MemoryStore())
        val originalStats = ArenaStats(1, 1, 0, 0, 1, 1)
        target.saveStats(originalStats)
        target.renameProfile(target.loadProfilesState().activeProfileId, "Keep Me")
        target.addHistory("Rock vs Scissors — Keep Me won")
        val originalHistory = target.loadHistory()
        val originalProfiles = target.loadProfilesState()

        val invalid = """
            RPS_ARENA_BACKUP_V2
            settings=true|false|true|true
            stats=2|2|0|0|2|2
            config=CLASSIC|CPU|NORMAL|BEST_OF_3|99|0
            activeProfile=profile-1
            profileIds=profile-1
            profile.profile-1=Replacement
            history=bad\q
        """.trimIndent()

        assertNull(target.previewBackup(invalid))
        assertFalse(target.importBackup(invalid))
        assertEquals(originalStats, target.loadStats())
        assertEquals(originalProfiles, target.loadProfilesState())
        assertEquals(originalHistory, target.loadHistory())
    }

    @Test
    fun duplicateBackupKeysAreRejectedWithoutMutation() {
        val target = ArenaRepository(MemoryStore())
        val original = ArenaStats(1, 1, 0, 0, 1, 1)
        target.saveStats(original)
        val duplicate = """
            RPS_ARENA_BACKUP_V1
            settings=false|true|false|true
            stats=0|0|0|0|0|0
            stats=1|1|0|0|1|1
            config=CLASSIC|CPU|NORMAL|BEST_OF_3|20260819|0
            history=
        """.trimIndent()

        assertNull(target.previewBackup(duplicate))
        assertFalse(target.importBackup(duplicate))
        assertEquals(original, target.loadStats())
    }

    @Test
    fun malformedBackupRowsAreRejectedWithoutMutation() {
        val target = ArenaRepository(MemoryStore())
        val original = ArenaStats(1, 1, 0, 0, 1, 1)
        target.saveStats(original)
        val malformed = """
            RPS_ARENA_BACKUP_V1
            settings=false|true|false|true
            this-row-has-no-separator
            stats=0|0|0|0|0|0
            config=CLASSIC|CPU|NORMAL|BEST_OF_3|20260819|0
            history=
        """.trimIndent()

        assertNull(target.previewBackup(malformed))
        assertFalse(target.importBackup(malformed))
        assertEquals(original, target.loadStats())
    }

    @Test
    fun oversizedBackupIsRejectedWithoutMutation() {
        val target = ArenaRepository(MemoryStore())
        val original = ArenaStats(1, 1, 0, 0, 1, 1)
        target.saveStats(original)
        val oversized = "RPS_ARENA_BACKUP_V2\n" + "x".repeat(40_000)

        assertNull(target.previewBackup(oversized))
        assertFalse(target.importBackup(oversized))
        assertEquals(original, target.loadStats())
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

    override fun remove(key: String) {
        values.remove(key)
    }

    fun containsKey(key: String): Boolean = key in values
}
