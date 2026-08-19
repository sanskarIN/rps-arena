package `in`.sanskar.rpsarena.logging

enum class LogLevel { DEBUG, INFO, WARN, ERROR }

data class LogEvent(
    val level: LogLevel,
    val event: String,
    val fields: Map<String, String>,
)

/**
 * Structured logger with key-based redaction. The default sink is no-op, so RPS Arena does not
 * create telemetry or upload logs. Future local/platform sinks can opt in without changing callers.
 */
class SafeLogger(private val sink: (LogEvent) -> Unit = {}) {
    fun debug(event: String, fields: Map<String, Any?> = emptyMap()) = emit(LogLevel.DEBUG, event, fields)
    fun info(event: String, fields: Map<String, Any?> = emptyMap()) = emit(LogLevel.INFO, event, fields)
    fun warn(event: String, fields: Map<String, Any?> = emptyMap()) = emit(LogLevel.WARN, event, fields)
    fun error(event: String, fields: Map<String, Any?> = emptyMap()) = emit(LogLevel.ERROR, event, fields)

    private fun emit(level: LogLevel, event: String, fields: Map<String, Any?>) {
        require(event.matches(EVENT_NAME)) { "Log event names must be lowercase snake_case" }
        val safeFields = fields.mapValues { (key, value) ->
            if (isSensitiveKey(key)) REDACTED else value?.toString().orEmpty().take(MAX_FIELD_LENGTH)
        }
        sink(LogEvent(level, event, safeFields))
    }

    private fun isSensitiveKey(key: String): Boolean {
        val normalized = key.lowercase()
        return SENSITIVE_KEY_FRAGMENTS.any(normalized::contains)
    }

    companion object {
        private const val REDACTED = "[REDACTED]"
        private const val MAX_FIELD_LENGTH = 160
        private val EVENT_NAME = Regex("[a-z][a-z0-9_]{1,63}")
        private val SENSITIVE_KEY_FRAGMENTS = setOf(
            "password",
            "passwd",
            "secret",
            "token",
            "authorization",
            "cookie",
            "email",
            "backup",
            "content",
            "payload",
        )
    }
}
