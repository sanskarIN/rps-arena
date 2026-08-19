package `in`.sanskar.rpsarena.ui

import `in`.sanskar.rpsarena.model.AppLanguage

data class AchievementCopy(
    val title: String,
    val description: String,
)

fun achievementCopy(id: String, language: AppLanguage): AchievementCopy = when (language) {
    AppLanguage.ENGLISH -> when (id) {
        "first_win" -> AchievementCopy("First Victory", "Win your first round")
        "ten_rounds" -> AchievementCopy("Arena Regular", "Play 10 rounds")
        "streak_3" -> AchievementCopy("On Fire", "Reach a 3-round win streak")
        "streak_7" -> AchievementCopy("Unstoppable", "Reach a 7-round win streak")
        "century" -> AchievementCopy("Century", "Play 100 rounds")
        else -> AchievementCopy("Achievement", "Complete the achievement requirement")
    }
    AppLanguage.HINDI -> when (id) {
        "first_win" -> AchievementCopy("पहली जीत", "अपना पहला राउंड जीतें")
        "ten_rounds" -> AchievementCopy("एरीना नियमित", "10 राउंड खेलें")
        "streak_3" -> AchievementCopy("जोश में", "लगातार 3 राउंड जीतें")
        "streak_7" -> AchievementCopy("अजेय", "लगातार 7 राउंड जीतें")
        "century" -> AchievementCopy("शतक", "100 राउंड खेलें")
        else -> AchievementCopy("उपलब्धि", "उपलब्धि की शर्त पूरी करें")
    }
}
