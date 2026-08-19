package `in`.sanskar.rpsarena

import `in`.sanskar.rpsarena.logging.LogEvent
import `in`.sanskar.rpsarena.logging.SafeLogger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SafeLoggerTest {
    @Test
    fun sensitiveFieldsAreRedactedBeforeSink() {
        val events = mutableListOf<LogEvent>()
        val logger = SafeLogger(events::add)

        logger.info(
            "backup_import_result",
            mapOf(
                "result" to "accepted",
                "email" to "person@example.com",
                "backupPayload" to "private backup text",
                "authToken" to "token-value",
            ),
        )

        val fields = events.single().fields
        assertEquals("accepted", fields["result"])
        assertEquals("[REDACTED]", fields["email"])
        assertEquals("[REDACTED]", fields["backupPayload"])
        assertEquals("[REDACTED]", fields["authToken"])
    }

    @Test
    fun longNonSensitiveFieldsAreBounded() {
        val events = mutableListOf<LogEvent>()
        val logger = SafeLogger(events::add)

        logger.debug("bounded_field", mapOf("detail" to "x".repeat(500)))

        assertEquals(160, events.single().fields.getValue("detail").length)
    }

    @Test
    fun invalidEventNameIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            SafeLogger().info("Backup Imported")
        }
    }
}
