package dev.sanskar.rpsarena.domain

object AchievementCatalog {
    fun evaluate(stats: GameStats): List<Achievement> = listOf(
        Achievement(
            id = "first_win",
            title = "First Victory",
            description = "Win your first round.",
            unlocked = stats.wins >= 1,
        ),
        Achievement(
            id = "streak_3",
            title = "On a Roll",
            description = "Reach a 3-round winning streak.",
            unlocked = stats.bestStreak >= 3,
        ),
        Achievement(
            id = "streak_10",
            title = "Unstoppable",
            description = "Reach a 10-round winning streak.",
            unlocked = stats.bestStreak >= 10,
        ),
        Achievement(
            id = "veteran_50",
            title = "Arena Veteran",
            description = "Play 50 rounds.",
            unlocked = stats.totalRounds >= 50,
        ),
        Achievement(
            id = "winner_100",
            title = "Century Club",
            description = "Win 100 rounds.",
            unlocked = stats.wins >= 100,
        ),
    )
}
