package io.qpointz.mill.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationIT {

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @Test
    fun `migrations apply cleanly and all expected tables exist`() {
        val tables = listOf(
            "chat_memory",
            "chat_memory_message",
            "ai_run_event",
            "ai_conversation",
            "ai_conversation_turn",
            "ai_artifact",
            "relation_record",
            "ai_active_artifact_pointer",
            "ai_chat_metadata",
        )
        tables.forEach { table ->
            val count = jdbc.queryForObject("SELECT COUNT(*) FROM $table", Int::class.java)
            assertThat(count).isNotNull().describedAs("table $table should exist and be queryable")
        }
    }
}
