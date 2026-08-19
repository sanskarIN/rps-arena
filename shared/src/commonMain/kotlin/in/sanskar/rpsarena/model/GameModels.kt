package `in`.sanskar.rpsarena.model

enum class Gesture(val emoji: String, val label: String) {
    ROCK("🪨", "Rock"),
    PAPER("📄", "Paper"),
    SCISSORS("✂️", "Scissors"),
    LIZARD("🦎", "Lizard"),
    SPOCK("🖖", "Spock");

    companion object {
        fun availableFor(variant: GameVariant): List<Gesture> = when (variant) {
            GameVariant.CLASSIC -> listOf(ROCK, PAPER, SCISSORS)
            GameVariant.LIZARD_SPOCK -> entries
        }
    }
}

enum class GameVariant { CLASSIC, LIZARD_SPOCK }
enum class OpponentMode { CPU, LOCAL_TWO_PLAYER }
enum class Difficulty { EASY, NORMAL, EXPERT }
enum class MatchMode { BEST_OF_3, BEST_OF_5, ENDLESS, STREAK, TOURNAMENT }
enum class RoundOutcome { PLAYER_ONE_WIN, PLAYER_TWO_WIN, DRAW }
enum class RoundEndReason { PLAYED, PLAYER_ONE_TIMEOUT, PLAYER_TWO_TIMEOUT }
enum class AppLanguage { ENGLISH, HINDI }

data class MatchConfig(
    val variant: GameVariant = GameVariant.CLASSIC,
    val opponentMode: OpponentMode = OpponentMode.CPU,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val matchMode: MatchMode = MatchMode.BEST_OF_3,
    val seed: Int = 20260819,
    val roundTimerSeconds: Int = 0,
) {
    init {
        require(roundTimerSeconds in ALLOWED_TIMER_SECONDS) {
            "Round timer must be one of ${ALLOWED_TIMER_SECONDS.joinToString()} seconds"
        }
    }

    val roundsToWin: Int? get() = when (matchMode) {
        MatchMode.BEST_OF_3 -> 2
        MatchMode.BEST_OF_5 -> 3
        MatchMode.TOURNAMENT -> 5
        MatchMode.ENDLESS, MatchMode.STREAK -> null
    }

    companion object {
        val ALLOWED_TIMER_SECONDS: Set<Int> = linkedSetOf(0, 5, 10, 20, 30, 60)
    }
}

data class RoundRecord(
    val playerOne: Gesture?,
    val playerTwo: Gesture?,
    val outcome: RoundOutcome,
    val endReason: RoundEndReason = RoundEndReason.PLAYED,
)

data class MatchSnapshot(
    val config: MatchConfig,
    val rounds: List<RoundRecord> = emptyList(),
    val playerOneScore: Int = 0,
    val playerTwoScore: Int = 0,
    val draws: Int = 0,
    val finished: Boolean = false,
)

data class ArenaStats(
    val roundsPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
) {
    val winRate: Int get() = if (roundsPlayed == 0) 0 else (wins * 100) / roundsPlayed
}

data class ArenaSettings(
    val darkTheme: Boolean = false,
    val followSystemTheme: Boolean = true,
    val reducedMotion: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val extendedVariant: Boolean = false,
    val onboardingComplete: Boolean = false,
    val playerName: String = "Player 1",
    val language: AppLanguage = AppLanguage.ENGLISH,
)

data class ArenaTrend(
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
) {
    val sampleSize: Int get() = wins + losses + draws
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
)
