package io.qpointz.mill.persistence.metadata.jpa.config

import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataAuditRepository
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaFacetRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaFacetTypeDefinitionRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataAuditRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataEntityRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataScopeRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataSeedLedgerRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaRuntimeFacetTypeRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataAuditJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeInstJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataSeedLedgerJpaRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Registers greenfield JPA adapters as Spring beans for applications that component-scan
 * `io.qpointz.mill.persistence.metadata.jpa` (including `testIT`).
 */
@Configuration
class MetadataJpaBeansConfiguration {

    @Bean
    fun facetTypeDefinitionRepository(
        jpa: MetadataFacetTypeJpaRepository
    ): FacetTypeDefinitionRepository = JpaFacetTypeDefinitionRepository(jpa)

    @Bean
    fun facetTypeRepository(
        inst: MetadataFacetTypeInstJpaRepository,
        defJpa: MetadataFacetTypeJpaRepository,
        definitionRepository: FacetTypeDefinitionRepository
    ): FacetTypeRepository {
        val defAdapter = definitionRepository as JpaFacetTypeDefinitionRepository
        return JpaRuntimeFacetTypeRepository(inst, defJpa, defAdapter)
    }

    @Bean
    fun metadataEntityRepository(
        jpa: MetadataEntityJpaRepository
    ): EntityRepository = JpaMetadataEntityRepository(jpa)

    @Bean
    fun facetRepository(
        facetJpa: MetadataFacetJpaRepository,
        entityJpa: MetadataEntityJpaRepository,
        typeJpa: MetadataFacetTypeInstJpaRepository,
        scopeJpa: MetadataScopeJpaRepository
    ): FacetRepository = JpaFacetRepository(facetJpa, entityJpa, typeJpa, scopeJpa)

    @Bean
    fun metadataScopeRepository(
        jpa: MetadataScopeJpaRepository
    ): MetadataScopeRepository = JpaMetadataScopeRepository(jpa)

    @Bean
    fun metadataAuditRepository(
        jpa: MetadataAuditJpaRepository
    ): MetadataAuditRepository = JpaMetadataAuditRepository(jpa)

    @Bean
    fun metadataSeedLedgerRepository(
        jpa: MetadataSeedLedgerJpaRepository
    ): MetadataSeedLedgerRepository = JpaMetadataSeedLedgerRepository(jpa)
}
