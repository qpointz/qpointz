package io.qpointz.mill.ai.autoconfigure

import io.qpointz.mill.ai.memory.ChatMemoryStore
import io.qpointz.mill.ai.persistence.ActiveArtifactPointerStore
import io.qpointz.mill.ai.persistence.ArtifactStore
import io.qpointz.mill.ai.persistence.ConversationStore
import io.qpointz.mill.ai.persistence.RunEventStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaActiveArtifactPointerStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaArtifactStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaChatMemoryStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaConversationStore
import io.qpointz.mill.persistence.ai.jpa.adapters.JpaRunEventStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

/**
 * Validates that MillAiV3AutoConfiguration registers JPA-backed ai/v3 stores when
 * mill-ai-v3-persistence is on the classpath and a JPA/Flyway stack is available.
 *
 * Acceptance: "adding persistence modules transparently switches to JPA-backed stores".
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MillAiV3JpaAutoConfigurationIT {

    @Autowired private lateinit var chatMemoryStore: ChatMemoryStore
    @Autowired private lateinit var runEventStore: RunEventStore
    @Autowired private lateinit var conversationStore: ConversationStore
    @Autowired private lateinit var artifactStore: ArtifactStore
    @Autowired private lateinit var activeArtifactPointerStore: ActiveArtifactPointerStore

    @Test
    fun `ChatMemoryStore is JPA-backed when persistence is on classpath`() {
        assertThat(chatMemoryStore).isInstanceOf(JpaChatMemoryStore::class.java)
    }

    @Test
    fun `RunEventStore is JPA-backed when persistence is on classpath`() {
        assertThat(runEventStore).isInstanceOf(JpaRunEventStore::class.java)
    }

    @Test
    fun `ConversationStore is JPA-backed when persistence is on classpath`() {
        assertThat(conversationStore).isInstanceOf(JpaConversationStore::class.java)
    }

    @Test
    fun `ArtifactStore is JPA-backed when persistence is on classpath`() {
        assertThat(artifactStore).isInstanceOf(JpaArtifactStore::class.java)
    }

    @Test
    fun `ActiveArtifactPointerStore is JPA-backed when persistence is on classpath`() {
        assertThat(activeArtifactPointerStore).isInstanceOf(JpaActiveArtifactPointerStore::class.java)
    }
}
