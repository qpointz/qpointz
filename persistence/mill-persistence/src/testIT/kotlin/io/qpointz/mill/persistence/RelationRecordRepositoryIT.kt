package io.qpointz.mill.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class RelationRecordRepositoryIT {

    @Autowired
    lateinit var repository: RelationRecordRepository

    @Test
    fun `should persist and reload a relation record`() {
        val record = RelationRecord(
            relationId = "rel-1",
            relationKind = "turn-to-artifact",
            sourceId = "turn-1",
            sourceType = "agent/conversation-turn",
            sourceUrn = "urn:agent/conversation-turn:turn-1",
            targetId = "art-1",
            targetType = "agent/artifact/sql-query",
            targetUrn = "urn:agent/artifact/sql-query:art-1",
            createdAt = Instant.now(),
        )
        repository.save(record)

        val found = repository.findByRelationKindAndSourceIdAndSourceType(
            "turn-to-artifact", "turn-1", "agent/conversation-turn"
        )
        assertThat(found).hasSize(1)
        assertThat(found[0].targetId).isEqualTo("art-1")
    }
}
