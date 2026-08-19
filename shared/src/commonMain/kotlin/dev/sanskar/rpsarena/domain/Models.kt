package dev.sanskar.rpsarena.domain

enum class Gesture(val label: String, val symbol: String) {
    ROCK("Rock", "✊"),
    PAPER("Paper", "✋"),
    SCISSORS("Scissors", "✌"),
    LIZARD("Lizard", "🦎"),
    SPOCK("Spock", "🖖"),
}

enum class GameVariant(val label: String) {
    CLASSIC("Classic"),
    LIZARD_SPOCK("Lizard–Spock");

    val gestures: List<Gesture>
        get() = when (this) {
            CLASSIC -> listOf(Gesture.ROCK, Gesture.PAPER, Gesture.SCISSORS)
            LIZARD_SPOCK -> Gesture.entries
        }
}

enum class OpponentMode(val label: String) {
    CPU("Vs CPU"),
    LOCAL_TWO_PLAYER("Two players"),
}

enum class Difficulty(val label: String, val description: String) {
    EASY("Easy", "Random valid moves"),
    NORMAL("Normal", "Mostly random with light adaptation"),
    EXPERT("Expert", "History-aware while preserving randomness"),
}

enum class GameMode(val label: String) {
    BEST_OF_3("Best of 3"),
    BEST_OF_5("Best of 5"),
    TOURNAMENT("Tournament"),
    ENDLESS("Endless"),
    STREAK("Streak"),
}

enum class RoundOutcome {
    PLAYER_ONE_WIN,
    PLAYER_TWO_WIN,
    DRAW,
}

enum class ThemePreference(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

data class RoundRecord(
    val roundNumber: Int,
    val playerOne: Gesture,
    val playerTwo: Gesture,
    val outcome: RoundOutcome,
)

data class GameStats(
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
) {
    val totalRounds: Int get() = wins + losses + draws
    val winRate: Int get() = if (totalRounds == 0) 0 else (wins * 100) / totalRounds
}

data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val reducedMotion: Boolean = false,
    val roundTimerSeconds: Int = 0,
    val variant: GameVariant = GameVariant.CLASSIC,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val gameMode: GameMode = GameMode.BEST_OF_3,
    val opponentMode: OpponentMode = OpponentMode.CPU,
    val seed: Long = 20260819L,
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
)

data class MatchState(
    val playerOneScore: Int = 0,
    val playerTwoScore: Int = 0,
    val draws: Int = 0,
    val roundNumber: Int = 1,
    val pendingPlayerOne: Gesture? = null,
    val lastRound: RoundRecord? = null,
    val finished: Boolean = false,
    val message: String = "Choose a move",
)
