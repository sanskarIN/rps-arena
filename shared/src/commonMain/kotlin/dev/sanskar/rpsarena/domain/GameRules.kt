package dev.sanskar.rpsarena.domain

object GameRules {
    private val beats: Map<Gesture, Set<Gesture>> = mapOf(
        Gesture.ROCK to setOf(Gesture.SCISSORS, Gesture.LIZARD),
        Gesture.PAPER to setOf(Gesture.ROCK, Gesture.SPOCK),
        Gesture.SCISSORS to setOf(Gesture.PAPER, Gesture.LIZARD),
        Gesture.LIZARD to setOf(Gesture.PAPER, Gesture.SPOCK),
        Gesture.SPOCK to setOf(Gesture.ROCK, Gesture.SCISSORS),
    )

    fun outcome(
        playerOne: Gesture,
        playerTwo: Gesture,
        variant: GameVariant,
    ): RoundOutcome {
        require(playerOne in variant.gestures) { "Player one gesture is not valid for ${variant.label}" }
        require(playerTwo in variant.gestures) { "Player two gesture is not valid for ${variant.label}" }

        return when {
            playerOne == playerTwo -> RoundOutcome.DRAW
            playerTwo in beats.getValue(playerOne) -> RoundOutcome.PLAYER_ONE_WIN
            else -> RoundOutcome.PLAYER_TWO_WIN
        }
    }

    fun counterFor(gesture: Gesture, variant: GameVariant): Gesture {
        return variant.gestures.first { candidate ->
            outcome(candidate, gesture, variant) == RoundOutcome.PLAYER_ONE_WIN
        }
    }

    fun requiredWins(mode: GameMode): Int? = when (mode) {
        GameMode.BEST_OF_3 -> 2
        GameMode.BEST_OF_5 -> 3
        GameMode.TOURNAMENT -> 5
        GameMode.ENDLESS,
        GameMode.STREAK,
        -> null
    }

    fun isMatchFinished(mode: GameMode, playerOneScore: Int, playerTwoScore: Int): Boolean {
        val required = requiredWins(mode) ?: return false
        return playerOneScore >= required || playerTwoScore >= required
    }
}
