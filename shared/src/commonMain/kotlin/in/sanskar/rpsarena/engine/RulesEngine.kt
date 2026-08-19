package in.sanskar.rpsarena.engine

import in.sanskar.rpsarena.model.Gesture
import in.sanskar.rpsarena.model.RoundOutcome

object RulesEngine {
    private val defeats: Map<Gesture, Set<Gesture>> = mapOf(
        Gesture.ROCK to setOf(Gesture.SCISSORS, Gesture.LIZARD),
        Gesture.PAPER to setOf(Gesture.ROCK, Gesture.SPOCK),
        Gesture.SCISSORS to setOf(Gesture.PAPER, Gesture.LIZARD),
        Gesture.LIZARD to setOf(Gesture.SPOCK, Gesture.PAPER),
        Gesture.SPOCK to setOf(Gesture.SCISSORS, Gesture.ROCK),
    )

    fun resolve(playerOne: Gesture, playerTwo: Gesture): RoundOutcome = when {
        playerOne == playerTwo -> RoundOutcome.DRAW
        playerTwo in defeats.getValue(playerOne) -> RoundOutcome.PLAYER_ONE_WIN
        else -> RoundOutcome.PLAYER_TWO_WIN
    }

    fun countersFor(gesture: Gesture): Set<Gesture> = defeats
        .filterValues { gesture in it }
        .keys
}
