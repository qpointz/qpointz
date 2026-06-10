package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.ai.persistence.RunEventRecord
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaRunEventStore
import io.qpointz.mill.persistence.ai.jpa.repositories.RunEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class JpaRunEventStoreIT {

    @Autowired lateinit var repo: RunEventRepository
    private val store by lazy { JpaRunEventStore(repo) }

    @Test
    fun `should save and reload run events`() {
        val record = RunEventRecord(
            eventId = "e1",
            runId = "run-1",
            conversationId = "c1",
            profileId = "p",
            kind = "llm",
            runtimeType = "llm.call.completed",
            content = mapOf("inputTokens" to 10, "outputTokens" to 20),
            createdAt = Instant.now(),
        )
        store.save(record)
        val results = store.findByRun("run-1")
        assertThat(results).hasSize(1)
        assertThat(results[0].eventId).isEqualTo("e1")
        assertThat(results[0].content["inputTokens"]).isEqualTo(10)
    }
}
