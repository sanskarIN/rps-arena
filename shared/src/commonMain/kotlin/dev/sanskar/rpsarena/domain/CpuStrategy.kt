package dev.sanskar.rpsarena.domain

class SeededRandom(seed: Long) {
    private var state: Long = if (seed == 0L) 0x5DEECE66DL else seed

    private fun nextLong(): Long {
        state = state * 6364136223846793005L + 1442695040888963407L
        return state
    }

    fun nextInt(bound: Int): Int {
        require(bound > 0)
        return ((nextLong() ushr 1) % bound.toLong()).toInt()
    }

    fun chance(percent: Int): Boolean {
        require(percent in 0..100)
        return nextInt(100) < percent
    }
}

class CpuStrategy(seed: Long) {
    private val random = SeededRandom(seed)

    fun choose(
        difficulty: Difficulty,
        variant: GameVariant,
        playerHistory: List<Gesture>,
    ): Gesture {
        val gestures = variant.gestures
        fun randomMove(): Gesture = gestures[random.nextInt(gestures.size)]

        return when (difficulty) {
            Difficulty.EASY -> randomMove()
            Difficulty.NORMAL -> {
                val last = playerHistory.lastOrNull()
                if (last != null && random.chance(30)) GameRules.counterFor(last, variant) else randomMove()
            }
            Difficulty.EXPERT -> {
                if (playerHistory.size >= 3 && random.chance(70)) {
                    val mostFrequent = gestures.maxBy { gesture -> playerHistory.count { it == gesture } }
                    GameRules.counterFor(mostFrequent, variant)
                } else {
                    randomMove()
                }
            }
        }
    }
}
