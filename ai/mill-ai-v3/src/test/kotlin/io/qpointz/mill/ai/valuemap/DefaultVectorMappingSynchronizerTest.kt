package io.qpointz.mill.ai.valuemap

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.embedding.EmbeddingModelPersistenceDescriptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DefaultVectorMappingSynchronizerTest {

    private val repo: ValueMappingEmbeddingRepository = mock()
    private val harness: EmbeddingHarness = mock()
    private val store = InMemoryEmbeddingStore<TextSegment>()

    private val sync = DefaultVectorMappingSynchronizer(repo, harness, store)

    @Test
    fun shouldUpsertAndAddToStore() {
        whenever(harness.dimension).thenReturn(2)
        whenever(harness.persistence).thenReturn(
            EmbeddingModelPersistenceDescriptor(
                "stub|2|text-embedding-3-small|default",
                "stub",
                "text-embedding-3-small",
                2,
                null,
                null,
            ),
        )
        whenever(harness.embed("a")).thenReturn(floatArrayOf(0.1f, 0.2f))
        whenever(repo.listValueRowsByAttributeUrn("urn:x")).thenReturn(emptyList())
        whenever(repo.findValueRow("urn:x", "a")).thenReturn(null)
        val sid = UUID.randomUUID()
        whenever(
            repo.upsertValueRow(
                isNull(),
                eq("urn:x"),
                eq("a"),
                isNull(),
                any(),
                eq(1L),
                isNull(),
            ),
        ).thenReturn(sid)

        sync.sync("urn:x", listOf(AttributeValueEntry("a")), 1L)

        verify(repo).upsertValueRow(isNull(), eq("urn:x"), eq("a"), isNull(), any(), eq(1L), isNull())
        val hits = store.search(
            EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(floatArrayOf(0.1f, 0.2f)))
                .maxResults(5)
                .build(),
        )
        assertThat(hits.matches().size).isPositive()
    }
}
