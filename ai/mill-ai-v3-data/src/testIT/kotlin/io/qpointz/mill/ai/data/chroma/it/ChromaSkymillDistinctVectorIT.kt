package io.qpointz.mill.ai.data.chroma.it

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.store.embedding.EmbeddingSearchRequest
import dev.langchain4j.store.embedding.EmbeddingStore
import io.qpointz.mill.ai.embedding.EmbeddingHarness
import io.qpointz.mill.ai.valuemap.AttributeValueEntry
import io.qpointz.mill.ai.valuemap.ValueMappingEmbeddingRepository
import io.qpointz.mill.ai.valuemap.ValueMappingService
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.proto.QueryExecutionConfig
import io.qpointz.mill.proto.QueryRequest
import io.qpointz.mill.proto.SQLStatement
import io.qpointz.mill.sql.RecordReaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Loads distinct Skymill values via SQL, runs [ValueMappingService] sync into Chroma, asserts similarity search.
 *
 * **Configuration:** Postgres, embedding profile (`mill.ai.embedding-model`), and Chroma wiring are defined under
 * profile `chroma-value-mapping-it-infra` in `application-chroma-explore-skymill.yml` (edit URL/credentials/model
 * there). Chroma HTTP API is hardcoded in [ChromaEmbeddingStoreItConfiguration].
 *
 * **Opt-in:** `MILL_CHROMA_IT_ENABLED=true` and a non-blank OpenAI credential (`OPENAI_API_KEY` from your
 * profile, or `MILL_AI_PROVIDERS_OPENAI_API_KEY` if you omit `api-key` in YAML).
 */
@SpringBootTest(classes = [ChromaSkymillValueMappingItApplication::class])
@ActiveProfiles("chroma-explore-skymill", "chroma-value-mapping-it-infra")
@EnabledIf("io.qpointz.mill.ai.data.chroma.it.ChromaSkymillVectorItConditions#enabled")
class ChromaSkymillDistinctVectorIT {

    @Autowired
    private lateinit var dataDispatcher: DataOperationDispatcher

    @Autowired
    private lateinit var valueMappingService: ValueMappingService

    @Autowired
    private lateinit var embeddingRepository: ValueMappingEmbeddingRepository

    @Autowired
    private lateinit var embeddingStore: EmbeddingStore<TextSegment>

    @Autowired
    private lateinit var embeddingHarness: EmbeddingHarness

    private val attributeUrn = "skymill.cities.state"
    private var embeddingModelId: Long = -1L

    // Idempotent insert by fingerprint — safe for @BeforeEach against a persistent DB.
    @BeforeEach
    fun insertModelRow() {
        val p = embeddingHarness.persistence
        embeddingModelId = embeddingRepository.ensureEmbeddingModel(
            configFingerprint = p.configFingerprint,
            provider = p.provider,
            modelId = p.modelId,
            dimension = p.dimension,
            paramsJson = p.paramsJson,
            label = p.label ?: "chroma-skymill-vector-it",
        )
    }

    @Test
    fun `distinct state values sync via ValueMappingService and similarity search in chroma`() {
        val sql = "SELECT DISTINCT `state` FROM `skymill`.`cities`"
        val request = QueryRequest.newBuilder()
            .setStatement(SQLStatement.newBuilder().setSql(sql).build())
            .setConfig(QueryExecutionConfig.newBuilder().setFetchSize(500).build())
            .build()

        val reader = RecordReaders.recordReader(dataDispatcher.execute(request))
        val distinctStates = mutableListOf<String>()
        while (reader.next()) {
            if (!reader.isNull(0)) {
                distinctStates.add(reader.getString(0))
            }
        }
        reader.close()

        assertThat(distinctStates).isNotEmpty()

        val entries = distinctStates.map { AttributeValueEntry(it, emptyMap()) }
        valueMappingService.syncAttribute(attributeUrn, entries, embeddingModelId)

        val probe = "New York"
        val probeEmbedding = embeddingHarness.embed(probe)

        val queryEmbedding = embeddingStore.search(
            EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(probeEmbedding))
                .maxResults(10)
                .minScore(0.0)
                .build(),
        )
        val texts = queryEmbedding.matches().map { it.embedded().text() }
        assertThat(texts).contains(probe)
    }
}
