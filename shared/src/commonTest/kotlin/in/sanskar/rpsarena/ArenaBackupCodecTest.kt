package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaBackup
import `in`.sanskar.rpsarena.data.ArenaBackupCodec
import `in`.sanskar.rpsarena.data.ArenaBackupDecodeResult
import `in`.sanskar.rpsarena.data.ArenaBackupError
import `in`.sanskar.rpsarena.model.ArenaHistoryEntry
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.RoundOutcome
import kotlin.test.Test
import kotlin.test.assertEquals

class ArenaBackupCodecTest {
    @Test
    fun backupRoundTripPreservesPortableData() {
        val backup = ArenaBackup(
            settings = ArenaSettings(
                darkTheme = true,
                followSystemTheme = false,
                reducedMotion = true,
                soundEnabled = false,
                hapticsEnabled = true,
                extendedVariant = true,
                onboardingComplete = true,
            ),
            stats = ArenaStats(
                roundsPlayed = 12,
                wins = 7,
                losses = 3,
                draws = 2,
                bestStreak = 4,
                currentStreak = 2,
            ),
            history = listOf(
                ArenaHistoryEntry.Round(Gesture.ROCK, Gesture.SCISSORS, RoundOutcome.PLAYER_ONE_WIN),
                ArenaHistoryEntry.Legacy("Paper | Spock — Draw 🖖"),
            ),
        )

        assertEquals(
            ArenaBackupDecodeResult.Success(backup),
            ArenaBackupCodec.decode(ArenaBackupCodec.encode(backup)),
        )
    }

    @Test
    fun encoderSanitizesAndLimitsHistory() {
        val history = (1..35).map { ArenaHistoryEntry.Legacy(" Round $it\nresult ") }
        val result = ArenaBackupCodec.decode(
            ArenaBackupCodec.encode(ArenaBackup(history = history)),
        )

        val expected = ArenaBackup(
            history = (1..ArenaBackupCodec.MAX_HISTORY_ITEMS).map { ArenaHistoryEntry.Legacy("Round $it result") },
        )
        assertEquals(ArenaBackupDecodeResult.Success(expected), result)
    }

    @Test
    fun decodesSchemaOneHistoryAsLegacyEntries() {
        val raw = """
            RPSARENA_BACKUP|1
            settings|false|true|false|true|true|false|true
            stats|2|1|0|1|1|0
            history|2
            item|Rock vs Scissors — Player 1 won
            item|Paper | Spock — Draw
        """.trimIndent()

        assertEquals(
            ArenaBackupDecodeResult.Success(
                ArenaBackup(
                    schemaVersion = 1,
                    settings = ArenaSettings(onboardingComplete = true),
                    stats = ArenaStats(2, 1, 0, 1, 1, 0),
                    history = listOf(
                        ArenaHistoryEntry.Legacy("Rock vs Scissors — Player 1 won"),
                        ArenaHistoryEntry.Legacy("Paper | Spock — Draw"),
                    ),
                ),
            ),
            ArenaBackupCodec.decode(raw),
        )
    }

    @Test
    fun rejectsFutureSchemaVersion() {
        val raw = """
            RPSARENA_BACKUP|3
            settings|false|true|false|true|true|false|false
            stats|0|0|0|0|0|0
            history|0
        """.trimIndent()

        assertEquals(
            ArenaBackupDecodeResult.Failure(ArenaBackupError.UNSUPPORTED_SCHEMA),
            ArenaBackupCodec.decode(raw),
        )
    }

    @Test
    fun rejectsMalformedStatsBeforeImport() {
        val raw = """
            RPSARENA_BACKUP|2
            settings|false|true|false|true|true|false|false
            stats|5|4|2|0|3|1
            history|0
        """.trimIndent()

        assertEquals(
            ArenaBackupDecodeResult.Failure(ArenaBackupError.MALFORMED_STATS),
            ArenaBackupCodec.decode(raw),
        )
    }

    @Test
    fun rejectsHistoryCountMismatch() {
        val raw = """
            RPSARENA_BACKUP|2
            settings|false|true|false|true|true|false|false
            stats|1|1|0|0|1|1
            history|2
            round|ROCK|SCISSORS|PLAYER_ONE_WIN
        """.trimIndent()

        assertEquals(
            ArenaBackupDecodeResult.Failure(ArenaBackupError.MALFORMED_HISTORY),
            ArenaBackupCodec.decode(raw),
        )
    }

    @Test
    fun rejectsMalformedStructuredHistoryEntry() {
        val raw = """
            RPSARENA_BACKUP|2
            settings|false|true|false|true|true|false|false
            stats|1|1|0|0|1|1
            history|1
            round|WATER|SCISSORS|PLAYER_ONE_WIN
        """.trimIndent()

        assertEquals(
            ArenaBackupDecodeResult.Failure(ArenaBackupError.MALFORMED_HISTORY),
            ArenaBackupCodec.decode(raw),
        )
    }

    @Test
    fun rejectsBlankBackup() {
        assertEquals(
            ArenaBackupDecodeResult.Failure(ArenaBackupError.EMPTY),
            ArenaBackupCodec.decode("  \n\r"),
        )
    }
}
