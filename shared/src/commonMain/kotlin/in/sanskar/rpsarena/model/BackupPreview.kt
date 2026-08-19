package `in`.sanskar.rpsarena.model

data class BackupPreview(
    val formatVersion: Int,
    val profileNames: List<String>,
    val activeProfileName: String,
    val stats: ArenaStats,
    val config: MatchConfig,
    val historyEntries: Int,
)
