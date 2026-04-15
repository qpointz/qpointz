package io.qpointz.mill.persistence.ai.jpa

import io.qpointz.mill.persistence.ai.jpa.adapters.JpaValueMappingEmbeddingAdapter
import io.qpointz.mill.persistence.ai.jpa.repositories.AiEmbeddingModelRepository
import io.qpointz.mill.persistence.ai.jpa.repositories.AiValueMappingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class JpaValueMappingEmbeddingAdapterIT {

    @Autowired
    private lateinit var modelRepo: AiEmbeddingModelRepository

    @Autowired
    private lateinit var valueRepo: AiValueMappingRepository

    private val repository by lazy { JpaValueMappingEmbeddingAdapter(modelRepo, valueRepo) }

    @Test
    fun shouldRoundTripModelAndValueRow() {
        val modelId = repository.ensureEmbeddingModel(
            configFingerprint = "test-fp-stub-384",
            provider = "stub",
            modelId = "deterministic",
            dimension = 2,
            paramsJson = null,
            label = "it",
        )
        assertThat(repository.findEmbeddingModelIdByFingerprint("test-fp-stub-384")).isEqualTo(modelId)

        val stableId = repository.upsertValueRow(
            stableId = null,
            attributeUrn = "urn:test:attr",
            content = "hello",
            contentHash = null,
            embedding = floatArrayOf(1f, 2f),
            embeddingModelId = modelId,
            metadataJson = """{"k":"v"}""",
        )

        val rows = repository.listValueRowsByAttributeUrn("urn:test:attr")
        assertThat(rows).hasSize(1)
        assertThat(rows[0].stableId).isEqualTo(stableId)
        assertThat(rows[0].embedding).containsExactly(1f, 2f)
        assertThat(rows[0].metadataJson).contains("k")

        repository.deleteValueRow(stableId)
        assertThat(repository.listValueRowsByAttributeUrn("urn:test:attr")).isEmpty()
    }

    @Test
    fun shouldEnsureEmbeddingModelReturnExistingIdWhenFingerprintAlreadyExists() {
        val fp = "test-fp-reuse-${System.nanoTime()}"
        val first = repository.ensureEmbeddingModel(
            configFingerprint = fp,
            provider = "stub",
            modelId = "deterministic",
            dimension = 2,
            paramsJson = null,
            label = "first",
        )
        val second = repository.ensureEmbeddingModel(
            configFingerprint = fp,
            provider = "stub",
            modelId = "deterministic",
            dimension = 2,
            paramsJson = null,
            label = "second-call-ignored",
        )
        assertThat(second).isEqualTo(first)
        assertThat(modelRepo.findAll().filter { it.configFingerprint == fp }).hasSize(1)
    }
}
