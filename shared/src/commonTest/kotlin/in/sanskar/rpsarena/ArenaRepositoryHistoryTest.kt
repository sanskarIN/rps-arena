package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaBackupImportResult
import `in`.sanskar.rpsarena.data.ArenaRepository
import `in`.sanskar.rpsarena.model.ArenaHistoryEntry
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.RoundOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArenaRepositoryHistoryTest {
    @Test
    fun legacyHistoryIsReadableBeforeMigration() {
        val store = TestArenaStore(
            mapOf("history_v1" to "Rock vs Scissors — Player 1 won\nPaper vs Rock — Player 1 won"),
        )
        val repository = ArenaRepository(store)

        assertEquals(
            listOf(
                ArenaHistoryEntry.Legacy("Rock vs Scissors — Player 1 won"),
                ArenaHistoryEntry.Legacy("Paper vs Rock — Player 1 won"),
            ),
            repository.loadHistoryEntries(),
        )
    }

    @Test
    fun firstStructuredRoundCarriesLegacyHistoryForward() {
        val store = TestArenaStore(
            mapOf("history_v1" to "Rock vs Paper — Player 2 won"),
        )
        val repository = ArenaRepository(store)
        val round = ArenaHistoryEntry.Round(
            Gesture.SPOCK,
            Gesture.SCISSORS,
            RoundOutcome.PLAYER_ONE_WIN,
        )

        repository.addHistoryEntry(round)

        assertEquals(
            listOf(round, ArenaHistoryEntry.Legacy("Rock vs Paper — Player 2 won")),
            repository.loadHistoryEntries(),
        )
        assertTrue(store["history_v2"].orEmpty().startsWith("RPSARENA_HISTORY|2"))
    }

    @Test
    fun malformedStructuredHistoryFallsBackToLegacyData() {
        val store = TestArenaStore(
            mapOf(
                "history_v2" to "RPSARENA_HISTORY|2\ncount|1\nround|WATER|ROCK|PLAYER_ONE_WIN",
                "history_v1" to "Paper vs Rock — Player 1 won",
            ),
        )
        val repository = ArenaRepository(store)

        assertEquals(
            listOf(ArenaHistoryEntry.Legacy("Paper vs Rock — Player 1 won")),
            repository.loadHistoryEntries(),
        )
    }

    @Test
    fun importingSchemaOneMigratesHistoryAndNextExportUsesSchemaTwo() {
        val repository = ArenaRepository(TestArenaStore())
        val schemaOne = """
            RPSARENA_BACKUP|1
            settings|false|true|false|true|true|false|true
            stats|1|1|0|0|1|1
            history|1
            item|Rock vs Scissors — Player 1 won
        """.trimIndent()

        assertEquals(ArenaBackupImportResult.Success(1), repository.importBackup(schemaOne))
        assertEquals(
            listOf(ArenaHistoryEntry.Legacy("Rock vs Scissors — Player 1 won")),
            repository.loadHistoryEntries(),
        )
        assertTrue(repository.exportBackup().startsWith("RPSARENA_BACKUP|2"))
    }

    @Test
    fun importingSchemaTwoPreservesStructuredRounds() {
        val repository = ArenaRepository(TestArenaStore())
        val schemaTwo = """
            RPSARENA_BACKUP|2
            settings|false|true|false|true|true|false|true
            stats|1|1|0|0|1|1
            history|1
            round|LIZARD|SPOCK|PLAYER_ONE_WIN
        """.trimIndent()

        assertEquals(ArenaBackupImportResult.Success(1), repository.importBackup(schemaTwo))
        assertEquals(
            listOf(
                ArenaHistoryEntry.Round(
                    Gesture.LIZARD,
                    Gesture.SPOCK,
                    RoundOutcome.PLAYER_ONE_WIN,
                ),
            ),
            repository.loadHistoryEntries(),
        )
    }
}
