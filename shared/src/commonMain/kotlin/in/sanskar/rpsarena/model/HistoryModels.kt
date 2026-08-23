package `in`.sanskar.rpsarena.model

/** Portable history representation that can be rendered in the current locale. */
sealed interface ArenaHistoryEntry {
    data class Round(
        val playerOne: Gesture,
        val playerTwo: Gesture,
        val outcome: RoundOutcome,
    ) : ArenaHistoryEntry

    /** Human-readable entry imported from the legacy history-v1 format. */
    data class Legacy(val summary: String) : ArenaHistoryEntry
}
