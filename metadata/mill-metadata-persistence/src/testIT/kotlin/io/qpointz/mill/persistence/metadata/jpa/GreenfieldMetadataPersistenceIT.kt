package io.qpointz.mill.persistence.metadata.jpa

import io.qpointz.mill.metadata.domain.MetadataEntity
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.FacetAssignment
import io.qpointz.mill.metadata.domain.facet.MergeAction
import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataAuditJpaRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Greenfield metadata DDL (V4) + JPA adapters: entity/facet persistence and listener-only
 * `metadata_audit` writes (WI-122 done criteria).
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class GreenfieldMetadataPersistenceIT {

    @Autowired
    private lateinit var metadataEntityRepository: EntityRepository

    @Autowired
    private lateinit var facetRepository: FacetRepository

    @Autowired
    private lateinit var auditJpa: MetadataAuditJpaRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `should persist entity facet and write audit row on insert`() {
        val id = "urn:mill/model/table:test.${UUID.randomUUID()}"
        val now = Instant.now()
        metadataEntityRepository.save(
            MetadataEntity(
                id = id,
                kind = "table",
                uuid = null,
                createdAt = now,
                createdBy = "it",
                lastModifiedAt = now,
                lastModifiedBy = "it"
            )
        )

        val facetUid = UUID.randomUUID().toString()
        val facet = FacetAssignment(
            uid = facetUid,
            entityId = id,
            facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
            scopeKey = MetadataUrns.SCOPE_GLOBAL,
            mergeAction = MergeAction.SET,
            payload = mapOf("displayName" to "T"),
            createdAt = now,
            createdBy = "it",
            lastModifiedAt = now,
            lastModifiedBy = "it"
        )
        facetRepository.save(facet)
        entityManager.flush()

        val loaded = metadataEntityRepository.findById(id)
        assertThat(loaded).isNotNull
        val facets = facetRepository.findByEntity(id)
        assertThat(facets).hasSize(1)
        assertThat(facets[0].payload["displayName"]).isEqualTo("T")

        assertThat(auditJpa.findBySubjectRef(id)).isNotEmpty
        assertThat(auditJpa.findBySubjectRef(facetUid).map { it.operation }).contains("FACET_ASSIGNED")
    }

    @Test
    fun `should append metadata_audit rows on facet update and delete`() {
        val id = "urn:mill/model/table:test.${UUID.randomUUID()}"
        val now = Instant.now()
        metadataEntityRepository.save(
            MetadataEntity(
                id = id,
                kind = "table",
                uuid = null,
                createdAt = now,
                createdBy = "it",
                lastModifiedAt = now,
                lastModifiedBy = "it"
            )
        )
        val facetUid = UUID.randomUUID().toString()
        facetRepository.save(
            FacetAssignment(
                uid = facetUid,
                entityId = id,
                facetTypeKey = MetadataUrns.FACET_TYPE_DESCRIPTIVE,
                scopeKey = MetadataUrns.SCOPE_GLOBAL,
                mergeAction = MergeAction.SET,
                payload = mapOf("displayName" to "Before"),
                createdAt = now,
                createdBy = "it",
                lastModifiedAt = now,
                lastModifiedBy = "it"
            )
        )
        entityManager.flush()

        val loaded = facetRepository.findByUid(facetUid)!!
        facetRepository.save(
            loaded.copy(
                payload = mapOf("displayName" to "After"),
                lastModifiedAt = Instant.now(),
                lastModifiedBy = "it2"
            )
        )
        entityManager.flush()
        assertThat(auditJpa.findBySubjectRef(facetUid).map { it.operation })
            .contains("FACET_ASSIGNED", "FACET_UPDATED")

        assertThat(facetRepository.deleteByUid(facetUid)).isTrue
        entityManager.flush()
        assertThat(auditJpa.findBySubjectRef(facetUid).map { it.operation })
            .contains("FACET_DELETED")
    }
}
