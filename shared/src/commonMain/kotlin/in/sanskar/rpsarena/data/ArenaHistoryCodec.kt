package `in`.sanskar.rpsarena.data

import `in`.sanskar.rpsarena.model.ArenaHistoryEntry
import `in`.sanskar.rpsarena.model.Gesture
import `in`.sanskar.rpsarena.model.RoundOutcome

internal object ArenaHistoryCodec {
    const val CURRENT_SCHEMA_VERSION = 2
    const val MAX_ITEMS = 30

    private const val MAGIC = "RPSARENA_HISTORY"
    private const val COUNT = "count"
    private const val ROUND = "round"
    private const val LEGACY = "legacy"

    fun encode(entries: List<ArenaHistoryEntry>): String {
        val normalized = entries
            .asSequence()
            .mapNotNull(::sanitize)
            .take(MAX_ITEMS)
            .toList()

        return buildString {
            append(MAGIC).append('|').append(CURRENT_SCHEMA_VERSION).append('\n')
            append(COUNT).append('|').append(normalized.size)
            normalized.forEach { entry ->
                append('\n')
                when (entry) {
                    is ArenaHistoryEntry.Round -> {
                        append(ROUND)
                            .append('|').append(entry.playerOne.name)
                            .append('|').append(entry.playerTwo.name)
                            .append('|').append(entry.outcome.name)
                    }
                    is ArenaHistoryEntry.Legacy -> {
                        append(LEGACY).append('|').append(escape(entry.summary))
                    }
                }
            }
        }
    }

    fun decode(raw: String): List<ArenaHistoryEntry>? {
        val normalized = raw.trimEnd('\r', '\n')
        if (normalized.isBlank()) return emptyList()

        val lines = normalized.lines().map { it.removeSuffix("\r") }
        if (lines.size < 2) return null

        val header = lines[0].split('|')
        if (
            header.size != 2 ||
            header[0] != MAGIC ||
            header[1].toIntOrNull() != CURRENT_SCHEMA_VERSION
        ) return null

        val countHeader = lines[1].split('|')
        if (countHeader.size != 2 || countHeader[0] != COUNT) return null
        val count = countHeader[1].toIntOrNull() ?: return null
        if (count !in 0..MAX_ITEMS || lines.size != count + 2) return null

        return lines.drop(2).map { line -> decodeEntry(line) ?: return null }
    }

    private fun decodeEntry(line: String): ArenaHistoryEntry? {
        val parts = line.split('|')
        return when (parts.firstOrNull()) {
            ROUND -> {
                if (parts.size != 4) return null
                val playerOne = Gesture.entries.firstOrNull { it.name == parts[1] } ?: return null
                val playerTwo = Gesture.entries.firstOrNull { it.name == parts[2] } ?: return null
                val outcome = RoundOutcome.entries.firstOrNull { it.name == parts[3] } ?: return null
                ArenaHistoryEntry.Round(playerOne, playerTwo, outcome)
            }
            LEGACY -> {
                if (parts.size != 2) return null
                val summary = unescape(parts[1]).trim()
                if (summary.isEmpty()) null else ArenaHistoryEntry.Legacy(summary)
            }
            else -> null
        }
    }

    private fun sanitize(entry: ArenaHistoryEntry): ArenaHistoryEntry? = when (entry) {
        is ArenaHistoryEntry.Round -> entry
        is ArenaHistoryEntry.Legacy -> entry.summary
            .replace('\r', ' ')
            .replace('\n', ' ')
            .trim()
            .takeIf { it.isNotEmpty() }
            ?.let(ArenaHistoryEntry::Legacy)
    }

    private fun escape(value: String): String = value
        .replace("%", "%25")
        .replace("|", "%7C")

    private fun unescape(value: String): String = value
        .replace("%7C", "|")
        .replace("%25", "%")
}
