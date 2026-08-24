package `in`.sanskar.rpsarena.ui

/** Stable semantic identifiers used by cross-platform UI automation. */
object ArenaUiTags {
    const val ONBOARDING_SCREEN = "arena.onboarding"
    const val ENTER_ARENA = "arena.onboarding.enter"

    const val HOME_SCREEN = "arena.home"
    const val HOME_PLAY = "arena.home.play"
    const val HOME_STATS = "arena.home.stats"
    const val HOME_HISTORY = "arena.home.history"
    const val HOME_ACHIEVEMENTS = "arena.home.achievements"
    const val HOME_ABOUT = "arena.home.about"
    const val TOP_SETTINGS = "arena.top.settings"

    const val PLAY_SCREEN = "arena.play"
    const val PLAY_BACK = "arena.play.back"
    const val GESTURE_ROCK = "arena.play.gesture.rock"
    const val GESTURE_PAPER = "arena.play.gesture.paper"
    const val GESTURE_SCISSORS = "arena.play.gesture.scissors"
    const val GESTURE_LIZARD = "arena.play.gesture.lizard"
    const val GESTURE_SPOCK = "arena.play.gesture.spock"
    const val LAST_ROUND = "arena.play.last_round"
    const val NEW_MATCH = "arena.play.new_match"

    const val STATS_SCREEN = "arena.stats"
    const val HISTORY_SCREEN = "arena.history"
    const val ACHIEVEMENTS_SCREEN = "arena.achievements"

    const val SETTINGS_SCREEN = "arena.settings"
    const val SETTINGS_REDUCED_MOTION = "arena.settings.reduced_motion"
    const val SETTINGS_EXPORT_BACKUP = "arena.settings.export_backup"
    const val SETTINGS_IMPORT_BACKUP = "arena.settings.import_backup"
    const val EXPORT_DIALOG = "arena.settings.export_dialog"
    const val IMPORT_DIALOG = "arena.settings.import_dialog"

    const val ABOUT_SCREEN = "arena.about"

    fun gesture(name: String): String = when (name) {
        "ROCK" -> GESTURE_ROCK
        "PAPER" -> GESTURE_PAPER
        "SCISSORS" -> GESTURE_SCISSORS
        "LIZARD" -> GESTURE_LIZARD
        "SPOCK" -> GESTURE_SPOCK
        else -> "arena.play.gesture.${name.lowercase()}"
    }
}
