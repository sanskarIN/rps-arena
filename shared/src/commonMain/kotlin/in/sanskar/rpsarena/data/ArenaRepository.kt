package `in`.sanskar.rpsarena.data

import `in`.sanskar.rpsarena.model.ArenaSettings
import `in`.sanskar.rpsarena.model.ArenaStats
import `in`.sanskar.rpsarena.model.BackupPreview
import `in`.sanskar.rpsarena.model.Difficulty
import `in`.sanskar.rpsarena.model.GameVariant
import `in`.sanskar.rpsarena.model.LocalProfile
import `in`.sanskar.rpsarena.model.LocalProfilesState
import `in`.sanskar.rpsarena.model.MatchConfig
import `in`.sanskar.rpsarena.model.MatchMode
import `in`.sanskar.rpsarena.model.OpponentMode

class ArenaRepository(private val store: KeyValueStore = DefaultKeyValueStore) {
    fun loadSettings(): ArenaSettings = decodeSettings(store.getString(KEY_SETTINGS))
    fun saveSettings(value: ArenaSettings) = store.putString(KEY_SETTINGS, encodeSettings(value))

    fun loadStats(): ArenaStats = decodeStats(store.getString(KEY_STATS))
    fun saveStats(value: ArenaStats) = store.putString(KEY_STATS, encodeStats(value))

    fun loadConfig(): MatchConfig = decodeConfig(store.getString(KEY_CONFIG))
    fun saveConfig(value: MatchConfig) = store.putString(KEY_CONFIG, encodeConfig(value))

    fun loadProfilesState(): LocalProfilesState {
        val ids = store.getString(KEY_PROFILE_IDS)
            .split('|')
            .map { it.trim() }
            .filter { it.matches(PROFILE_ID_PATTERN) }
            .distinct()
            .take(MAX_PROFILES)

        val profiles = ids.mapNotNull { id ->
            normalizeProfileName(store.getString(profileNameKey(id)))?.let { name ->
                LocalProfile(id = id, displayName = name)
            }
        }.ifEmpty { listOf(LocalProfilesState.DEFAULT_LOCAL_PROFILE) }

        val requestedActive = store.getString(KEY_ACTIVE_PROFILE)
        val activeId = requestedActive.takeIf { requested -> profiles.any { it.id == requested } }
            ?: profiles.first().id
        return LocalProfilesState(profiles = profiles, activeProfileId = activeId)
    }

    fun createProfile(displayName: String): LocalProfilesState? {
        val name = normalizeProfileName(displayName) ?: return null
        val current = loadProfilesState()
        if (current.profiles.size >= MAX_PROFILES) return null
        val id = generateProfileId(current.profiles)
        val updated = LocalProfilesState(
            profiles = current.profiles + LocalProfile(id, name),
            activeProfileId = id,
        )
        saveProfilesState(updated)
        return updated
    }

    fun renameProfile(profileId: String, displayName: String): LocalProfilesState? {
        val name = normalizeProfileName(displayName) ?: return null
        val current = loadProfilesState()
        if (current.profiles.none { it.id == profileId }) return null
        val updated = current.copy(
            profiles = current.profiles.map { profile ->
                if (profile.id == profileId) profile.copy(displayName = name) else profile
            },
        )
        saveProfilesState(updated)
        return updated
    }

    fun activateProfile(profileId: String): LocalProfilesState? {
        val current = loadProfilesState()
        if (current.profiles.none { it.id == profileId }) return null
        val updated = current.copy(activeProfileId = profileId)
        saveProfilesState(updated)
        return updated
    }

    fun deleteProfile(profileId: String): LocalProfilesState? {
        val current = loadProfilesState()
        if (current.profiles.size <= 1 || current.profiles.none { it.id == profileId }) return null
        val profiles = current.profiles.filterNot { it.id == profileId }
        val updated = LocalProfilesState(
            profiles = profiles,
            activeProfileId = if (current.activeProfileId == profileId) profiles.first().id else current.activeProfileId,
        )
        saveProfilesState(updated)
        return updated
    }

    fun loadHistory(): List<String> = store.getString(KEY_HISTORY)
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(MAX_HISTORY)
        .toList()

    fun addHistory(line: String) {
        val sanitized = line.replace('\n', ' ').replace('\r', ' ').trim().take(MAX_HISTORY_LINE_LENGTH)
        if (sanitized.isEmpty()) return
        val updated = (listOf(sanitized) + loadHistory()).take(MAX_HISTORY)
        store.putString(KEY_HISTORY, updated.joinToString("\n"))
    }

    fun replaceHistory(lines: List<String>): Boolean {
        if (lines.size > MAX_HISTORY) return false
        if (lines.any { line ->
                line.isBlank() ||
                    line.length > MAX_HISTORY_LINE_LENGTH ||
                    line.any { it == '\n' || it == '\r' || it.code < 0x20 && it != '\t' }
            }) return false
        store.putString(KEY_HISTORY, lines.joinToString("\n"))
        return true
    }

    fun clearHistory() {
        store.putString(KEY_HISTORY, "")
    }

    fun resetAll() {
        saveSettings(ArenaSettings())
        saveStats(ArenaStats())
        saveConfig(MatchConfig())
        saveProfilesState(LocalProfilesState.default())
        clearHistory()
    }

    fun exportBackup(): String {
        val profilesState = loadProfilesState()
        return buildString {
            appendLine(BACKUP_HEADER_V2)
            appendLine("settings=${encodeSettings(loadSettings())}")
            appendLine("stats=${encodeStats(loadStats())}")
            appendLine("config=${encodeConfig(loadConfig())}")
            appendLine("activeProfile=${profilesState.activeProfileId}")
            appendLine("profileIds=${profilesState.profiles.joinToString("|") { it.id }}")
            profilesState.profiles.forEach { profile ->
                appendLine("profile.${profile.id}=${escapeBackupValue(profile.displayName)}")
            }
            append("history=${loadHistory().joinToString("\t") { escapeHistory(it) }}")
        }
    }

    fun previewBackup(raw: String): BackupPreview? {
        val decoded = decodeBackup(raw) ?: return null
        return BackupPreview(
            formatVersion = decoded.formatVersion,
            profileNames = decoded.profilesState.profiles.map { it.displayName },
            activeProfileName = decoded.profilesState.activeProfile.displayName,
            stats = decoded.stats,
            config = decoded.config,
            historyEntries = decoded.history.size,
        )
    }

    fun importBackup(raw: String): Boolean {
        val decoded = decodeBackup(raw) ?: return false
        saveSettings(decoded.settings)
        saveStats(decoded.stats)
        saveConfig(decoded.config)
        saveProfilesState(decoded.profilesState)
        return replaceHistory(decoded.history)
    }

    internal fun encodeSettings(value: ArenaSettings): String = listOf(
        value.darkTheme,
        value.followSystemTheme,
        value.reducedMotion,
        value.onboardingComplete,
    ).joinToString("|")

    internal fun decodeSettings(raw: String): ArenaSettings = decodeSettingsOrNull(raw) ?: ArenaSettings()

    private fun decodeSettingsOrNull(raw: String): ArenaSettings? {
        val parts = raw.split('|')
        return when (parts.size) {
            4 -> {
                val values = parts.map { it.toBooleanStrictOrNull() ?: return null }
                ArenaSettings(
                    darkTheme = values[0],
                    followSystemTheme = values[1],
                    reducedMotion = values[2],
                    onboardingComplete = values[3],
                )
            }
            7 -> {
                val legacy = parts.map { it.toBooleanStrictOrNull() ?: return null }
                ArenaSettings(
                    darkTheme = legacy[0],
                    followSystemTheme = legacy[1],
                    reducedMotion = legacy[2],
                    onboardingComplete = legacy[6],
                )
            }
            else -> null
        }
    }

    internal fun encodeStats(value: ArenaStats): String = listOf(
        value.roundsPlayed,
        value.wins,
        value.losses,
        value.draws,
        value.bestStreak,
        value.currentStreak,
    ).joinToString("|")

    internal fun decodeStats(raw: String): ArenaStats = decodeStatsOrNull(raw) ?: ArenaStats()

    private fun decodeStatsOrNull(raw: String): ArenaStats? {
        val p = raw.split('|').map { it.toIntOrNull() ?: return null }
        if (p.size != 6 || p.any { it < 0 }) return null
        if (p[0] != p[1] + p[2] + p[3]) return null
        if (p[5] > p[4]) return null
        return ArenaStats(p[0], p[1], p[2], p[3], p[4], p[5])
    }

    internal fun encodeConfig(value: MatchConfig): String = listOf(
        value.variant.name,
        value.opponentMode.name,
        value.difficulty.name,
        value.matchMode.name,
        value.seed,
        value.roundTimerSeconds,
    ).joinToString("|")

    internal fun decodeConfig(raw: String): MatchConfig = decodeConfigOrNull(raw) ?: MatchConfig()

    private fun decodeConfigOrNull(raw: String): MatchConfig? {
        val p = raw.split('|')
        if (p.size != 6) return null
        return runCatching {
            val timer = p[5].toInt()
            require(timer in 0..60)
            MatchConfig(
                variant = GameVariant.valueOf(p[0]),
                opponentMode = OpponentMode.valueOf(p[1]),
                difficulty = Difficulty.valueOf(p[2]),
                matchMode = MatchMode.valueOf(p[3]),
                seed = p[4].toInt(),
                roundTimerSeconds = timer,
            )
        }.getOrNull()
    }

    private fun decodeBackup(raw: String): DecodedBackup? {
        if (raw.length > MAX_BACKUP_LENGTH) return null
        val lines = raw.replace("\r\n", "\n").trim().split('\n')
        val header = lines.firstOrNull() ?: return null
        val formatVersion = when (header) {
            BACKUP_HEADER_V1 -> 1
            BACKUP_HEADER_V2 -> 2
            else -> return null
        }

        val values = lines.drop(1).mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) null else line.substring(0, separator) to line.substring(separator + 1)
        }.toMap()

        val settings = decodeSettingsOrNull(values["settings"].orEmpty()) ?: return null
        val stats = decodeStatsOrNull(values["stats"].orEmpty()) ?: return null
        val config = decodeConfigOrNull(values["config"].orEmpty()) ?: return null
        val historyRaw = values["history"] ?: return null
        val history = if (historyRaw.isBlank()) {
            emptyList()
        } else {
            historyRaw.split('\t').map { unescapeHistory(it) ?: return null }
        }
        if (history.size > MAX_HISTORY || history.any { it.length > MAX_HISTORY_LINE_LENGTH }) return null

        val profilesState = if (formatVersion == 2) {
            decodeBackupProfiles(values) ?: return null
        } else {
            LocalProfilesState.default()
        }
        return DecodedBackup(formatVersion, settings, stats, config, profilesState, history)
    }

    private fun saveProfilesState(value: LocalProfilesState) {
        val validProfiles = value.profiles
            .filter { it.id.matches(PROFILE_ID_PATTERN) }
            .mapNotNull { profile ->
                normalizeProfileName(profile.displayName)?.let { name -> profile.copy(displayName = name) }
            }
            .distinctBy { it.id }
            .take(MAX_PROFILES)
            .ifEmpty { listOf(LocalProfilesState.DEFAULT_LOCAL_PROFILE) }
        val activeId = value.activeProfileId.takeIf { id -> validProfiles.any { it.id == id } }
            ?: validProfiles.first().id
        store.putString(KEY_PROFILE_IDS, validProfiles.joinToString("|") { it.id })
        validProfiles.forEach { profile -> store.putString(profileNameKey(profile.id), profile.displayName) }
        store.putString(KEY_ACTIVE_PROFILE, activeId)
    }

    private fun decodeBackupProfiles(values: Map<String, String>): LocalProfilesState? {
        val ids = values["profileIds"]
            ?.split('|')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: return null
        if (ids.isEmpty() || ids.size > MAX_PROFILES || ids.distinct().size != ids.size) return null
        if (ids.any { !it.matches(PROFILE_ID_PATTERN) }) return null

        val profiles = ids.map { id ->
            val rawName = values["profile.$id"] ?: return null
            val name = unescapeBackupValue(rawName)?.let(::normalizeProfileName) ?: return null
            LocalProfile(id, name)
        }
        val activeId = values["activeProfile"] ?: return null
        if (profiles.none { it.id == activeId }) return null
        return LocalProfilesState(profiles, activeId)
    }

    private fun generateProfileId(profiles: List<LocalProfile>): String {
        val existing = profiles.map { it.id }.toSet()
        var suffix = 1
        while (suffix <= MAX_PROFILE_ID_SUFFIX) {
            val candidate = "profile-$suffix"
            if (candidate !in existing) return candidate
            suffix += 1
        }
        return "profile-${MAX_PROFILE_ID_SUFFIX + 1}"
    }

    private fun normalizeProfileName(raw: String): String? {
        if (raw.any { it == '\n' || it == '\r' || it.code < 0x20 }) return null
        val name = raw.trim().replace(Regex("\\s+"), " ")
        if (name.length !in 1..MAX_PROFILE_NAME_LENGTH) return null
        return name
    }

    private fun profileNameKey(id: String): String = "$KEY_PROFILE_NAME_PREFIX$id"

    private fun escapeHistory(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\t", "\\t")

    private fun unescapeHistory(value: String): String? {
        val result = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            val character = value[index]
            if (character != '\\') {
                result.append(character)
                index += 1
                continue
            }
            if (index + 1 >= value.length) return null
            when (value[index + 1]) {
                '\\' -> result.append('\\')
                't' -> result.append('\t')
                else -> return null
            }
            index += 2
        }
        return result.toString()
    }

    private fun escapeBackupValue(value: String): String = buildString(value.length) {
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(character)
            }
        }
    }

    private fun unescapeBackupValue(value: String): String? {
        val result = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            val character = value[index]
            if (character != '\\') {
                result.append(character)
                index += 1
                continue
            }
            if (index + 1 >= value.length) return null
            when (value[index + 1]) {
                '\\' -> result.append('\\')
                'n' -> result.append('\n')
                'r' -> result.append('\r')
                else -> return null
            }
            index += 2
        }
        return result.toString()
    }

    companion object {
        const val MAX_HISTORY = 30
        const val MAX_PROFILES = 6
        const val MAX_PROFILE_NAME_LENGTH = 24
        private const val MAX_PROFILE_ID_SUFFIX = 9_999
        private const val MAX_HISTORY_LINE_LENGTH = 240
        private const val MAX_BACKUP_LENGTH = 32_768
        private const val BACKUP_HEADER_V1 = "RPS_ARENA_BACKUP_V1"
        private const val BACKUP_HEADER_V2 = "RPS_ARENA_BACKUP_V2"
        private const val KEY_SETTINGS = "settings_v1"
        private const val KEY_STATS = "stats_v1"
        private const val KEY_CONFIG = "config_v1"
        private const val KEY_HISTORY = "history_v1"
        private const val KEY_PROFILE_IDS = "profiles_v1"
        private const val KEY_ACTIVE_PROFILE = "active_profile_v1"
        private const val KEY_PROFILE_NAME_PREFIX = "profile_name_v1:"
        private val PROFILE_ID_PATTERN = Regex("profile-[1-9][0-9]{0,4}")
    }
}

private data class DecodedBackup(
    val formatVersion: Int,
    val settings: ArenaSettings,
    val stats: ArenaStats,
    val config: MatchConfig,
    val profilesState: LocalProfilesState,
    val history: List<String>,
)
