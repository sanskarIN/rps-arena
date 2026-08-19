package in.sanskar.rpsarena.engine

import in.sanskar.rpsarena.model.Difficulty
import in.sanskar.rpsarena.model.GameVariant
import in.sanskar.rpsarena.model.Gesture
import kotlin.random.Random

class CpuStrategy(seed: Int) {
    private val random = Random(seed)

    fun choose(
        difficulty: Difficulty,
        variant: GameVariant,
        playerHistory: List<Gesture>,
    ): Gesture {
        val allowed = Gesture.availableFor(variant)
        return when (difficulty) {
            Difficulty.EASY -> allowed.random(random)
            Difficulty.NORMAL -> chooseNormal(allowed, playerHistory)
            Difficulty.EXPERT -> chooseExpert(allowed, playerHistory)
        }
    }

    private fun chooseNormal(allowed: List<Gesture>, history: List<Gesture>): Gesture {
        if (history.size < 3 || random.nextInt(100) < 55) return allowed.random(random)
        val last = history.last()
        val counters = RulesEngine.countersFor(last).filter { it in allowed }
        return counters.ifEmpty { allowed }.random(random)
    }

    private fun chooseExpert(allowed: List<Gesture>, history: List<Gesture>): Gesture {
        if (history.size < 5 || random.nextInt(100) < 20) return allowed.random(random)
        val predicted = history
            .filter { it in allowed }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: return allowed.random(random)
        val counters = RulesEngine.countersFor(predicted).filter { it in allowed }
        return counters.ifEmpty { allowed }.random(random)
    }
}
