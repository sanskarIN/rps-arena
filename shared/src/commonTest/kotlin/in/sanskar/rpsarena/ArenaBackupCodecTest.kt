package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.data.ArenaBackup
import `in`.sanskar.rpsarena.data.ArenaBackupCodec
import `in`.sanskar.rpsarena.data.ArenaBackupDecodeResult
import `in`.sanskar.rpsarena.data.ArenaBackupError
import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
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
                "Rock vs Scissors — Player 1 won",
                "Paper | Spock — Draw 🖖",
            ),
        )

        assertEquals(
            ArenaBackupDecodeResult.Success(backup),
            ArenaBackupCodec.decode(ArenaBackupCodec.encode(backup)),
        )
    }

    @Test
    fun encoderSanitizesAndLimitsHistory() {
        val history = (1..35).map { " Round $it\nresult " }
        val result = ArenaBackupCodec.decode(
            ArenaBackupCodec.encode(ArenaBackup(history = history)),
        )

        val expected = ArenaBackup(
            history = (1..ArenaBackupCodec.MAX_HISTORY_ITEMS).map { "Round $it result" },
        )
        assertEquals(ArenaBackupDecodeResult.Success(expected), result)
    }

    @Test
    fun rejectsFutureSchemaVersion() {
        val raw = """
            RPSARENA_BACKUP|2
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
            RPSARENA_BACKUP|1
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
            RPSARENA_BACKUP|1
            settings|false|true|false|true|true|false|false
            stats|1|1|0|0|1|1
            history|2
            item|Rock vs Scissors — Player 1 won
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
