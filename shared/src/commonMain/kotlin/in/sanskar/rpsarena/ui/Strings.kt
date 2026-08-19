package `in`.sanskar.rpsarena.ui

import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.LocalTurnPhase
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.RoundOutcome

/**
 * English UI catalog. Keeping product copy outside composables makes later localization possible
 * without changing game/state logic. A future locale layer can replace this object with resources.
 */
object Strings {
    const val appName = "RPS Arena"
    const val settings = "Settings"
    const val welcomeTitle = "Welcome to RPS Arena"
    const val welcomeBody = "Fast, offline-first matches with transparent CPU difficulty and an optional Lizard–Spock ruleset."
    const val enterArena = "Enter the Arena"
    const val chooseArena = "Choose your arena"
    const val homeSubtitle = "Classic strategy. No account. No network required."
    const val play = "Play"
    const val stats = "Stats"
    const val history = "History"
    const val achievements = "Achievements"
    const val about = "About"
    const val madeBy = "Made by the Sanskar"
    const val back = "← Back"

    const val localProfiles = "Local profiles"
    const val localProfilesSummary = "Profiles stay on this device. They are not accounts and are never uploaded by RPS Arena."
    const val activeProfile = "Active player"
    const val profileName = "Player name"
    const val profileNameHelp = "1–24 characters; stored only on this device and included in backups."
    const val createProfile = "Create profile"
    const val renameProfile = "Rename active profile"
    const val deleteProfile = "Delete active profile"
    const val profileCreated = "Local profile created and selected."
    const val profileRenamed = "Active profile renamed."
    const val profileDeleted = "Active profile deleted."
    const val profileRejected = "Use a valid player name and keep no more than 6 local profiles."
    const val profileDeleteRejected = "At least one local profile must remain."

    const val opponent = "Opponent"
    const val cpu = "CPU"
    const val twoPlayer = "2 Player"
    const val rules = "Rules"
    const val classic = "Classic"
    const val lizardSpock = "Lizard–Spock"
    const val difficulty = "Difficulty"
    const val mode = "Mode"
    const val roundTimer = "Round timer"
    const val timerOff = "Off"
    const val seedTitle = "Replayable CPU seed"
    const val seedLabel = "Seed"
    const val seedHelp = "Whole number; changing it restarts the current match."
    const val seedInvalid = "Enter a valid whole-number seed."
    const val seedApplied = "Seed applied."
    const val chooseGesture = "Choose a gesture"
    const val lastRound = "Last round"
    const val draw = "Draw"
    const val playerOneRoundWin = "Player 1 wins the round"
    const val playerTwoRoundWin = "Player 2 wins the round"
    const val cpuRoundWin = "CPU wins the round"
    const val newMatch = "New match"
    const val restartMatch = "Restart match"
    const val playerOneShort = "P1"
    const val playerTwoShort = "P2"
    const val draws = "Draws"
    const val clear = "Clear"
    const val noHistory = "No rounds yet. Your latest 30 rounds stay on this device."

    const val rounds = "Rounds"
    const val wins = "Wins"
    const val losses = "Losses"
    const val winRate = "Win rate"
    const val currentStreak = "Current streak"
    const val bestStreak = "Best streak"
    const val recentTrend = "Recent trend"
    const val recentTrendEmpty = "Play a round to build a recent-results trend."
    const val recentTrendLegend = "W = win · L = loss · D = draw"
    const val recentDecisiveWinRate = "Recent decisive win rate"

    const val appearanceAccessibility = "Appearance & accessibility"
    const val followSystemTheme = "Follow system theme"
    const val darkTheme = "Dark theme"
    const val reducedMotion = "Reduced motion"
    const val accessibilitySummary = "Core game actions use text labels, large touch targets, keyboard-compatible controls, and non-color-only results."
    const val privacyLocalData = "Privacy & local data"
    const val privacySummary = "No account or cloud sync is required. Settings, local profiles, statistics, and recent history remain on this device."
    const val generateBackup = "Generate backup"
    const val backupGenerated = "Backup generated below."
    const val backupField = "RPS Arena backup text"
    const val backupHelp = "Versioned plain text; do not use this field for secrets."
    const val importBackup = "Import backup"
    const val backupImported = "Backup imported successfully."
    const val backupRejected = "Backup rejected: unsupported or malformed data."
    const val clearRecentHistory = "Clear recent history"
    const val historyCleared = "Recent history cleared. Lifetime statistics were kept."
    const val resetAllData = "Reset all local data"
    const val resetConfirmation = "Reset settings, profiles, statistics, match setup, and history on this device?"
    const val confirmReset = "Confirm reset"
    const val cancel = "Cancel"
    const val resetComplete = "All local RPS Arena data reset."
    const val updatesProject = "Updates & project"
    const val updatesSummary = "Updates are distributed through project releases. RPS Arena does not silently install updates or require background network access."
    const val openAboutSupport = "Open About & support"

    const val aboutTitle = "About RPS Arena"
    const val versionLicense = "Version 1.0.0 · MIT License"
    const val aboutDescription = "An open-source, offline-first Rock Paper Scissors game for Android and desktop."
    const val aboutExtended = "Optional extended rules: Rock–Paper–Scissors–Lizard–Spock."
    const val aboutCpu = "CPU modes are local and deterministic from a seed; no hidden online model is used."
    const val repository = "Repository"
    const val githubProfile = "GitHub profile"
    const val buyMeCoffee = "Buy Me a Coffee"
    const val businessOutlook = "Business · sanskarin@outlook.in"
    const val businessGmail = "Business · sanskarin.business@gmail.com"
    const val supportEmail = "Support · supportramsandesh@gmail.com"
    const val fundingOptional = "Funding is optional; every game feature remains usable without donating."

    fun profileChip(name: String, active: Boolean): String = if (active) "$name · active" else name
    fun chooseGestureAccessibility(label: String): String = "Choose $label"
    fun timerSeconds(seconds: Int): String = "${seconds}s"
    fun turnTimer(seconds: Int): String =
        "Turn timer: ${seconds}s. At zero, a deterministic valid gesture is selected from the active seed."

    fun localTurnMessage(phase: LocalTurnPhase): String = when (phase) {
        LocalTurnPhase.PLAYER_ONE_CHOOSE -> "Player 1: choose secretly"
        LocalTurnPhase.PLAYER_TWO_CHOOSE -> "Player 2: choose now — Player 1 move is hidden"
    }

    fun achievementTitle(id: String): String = when (id) {
        "first_win" -> "First Victory"
        "ten_rounds" -> "Arena Regular"
        "streak_3" -> "On Fire"
        "streak_7" -> "Unstoppable"
        "century" -> "Century"
        else -> "Achievement"
    }

    fun achievementDescription(id: String): String = when (id) {
        "first_win" -> "Win your first round"
        "ten_rounds" -> "Play 10 rounds"
        "streak_3" -> "Reach a 3-round win streak"
        "streak_7" -> "Reach a 7-round win streak"
        "century" -> "Play 100 rounds"
        else -> "Complete an arena challenge"
    }

    fun difficultyLabel(value: Difficulty): String = when (value) {
        Difficulty.EASY -> "Easy"
        Difficulty.NORMAL -> "Normal"
        Difficulty.EXPERT -> "Expert"
    }

    fun matchModeLabel(value: MatchMode): String = when (value) {
        MatchMode.BEST_OF_3 -> "Best of 3"
        MatchMode.BEST_OF_5 -> "Best of 5"
        MatchMode.ENDLESS -> "Endless"
        MatchMode.STREAK -> "Streak"
        MatchMode.TOURNAMENT -> "Tournament"
    }

    fun outcomeLabel(outcome: RoundOutcome, cpuOpponent: Boolean): String = when (outcome) {
        RoundOutcome.PLAYER_ONE_WIN -> playerOneRoundWin
        RoundOutcome.PLAYER_TWO_WIN -> if (cpuOpponent) cpuRoundWin else playerTwoRoundWin
        RoundOutcome.DRAW -> draw
    }
}
