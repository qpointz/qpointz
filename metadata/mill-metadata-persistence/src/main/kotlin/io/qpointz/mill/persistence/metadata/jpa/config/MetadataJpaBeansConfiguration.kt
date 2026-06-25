package io.qpointz.mill.persistence.metadata.jpa.config

import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataAuditRepository
import io.qpointz.mill.metadata.repository.EntityRepository
import io.qpointz.mill.metadata.repository.MetadataContentRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaFacetRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaFacetTypeDefinitionRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataAuditRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataContentRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataEntityRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataScopeRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaMetadataSeedLedgerRepository
import io.qpointz.mill.persistence.metadata.jpa.adapters.JpaRuntimeFacetTypeRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataAuditJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataContentJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataEntityJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeInstJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetTypeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataScopeJpaRepository
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataSeedLedgerJpaRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Registers greenfield JPA adapters when `mill.metadata.repository.type=jpa`.
 *
 * Imported by [io.qpointz.mill.metadata.configuration.MetadataJpaPersistenceAutoConfiguration]
 * and guarded by property so broad `@ComponentScan("io.qpointz")` does not register JPA repos
 * alongside file or no-op backends.
 */
@Configuration
@ConditionalOnProperty(prefix = "mill.metadata.repository", name = ["type"], havingValue = "jpa")
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
    fun metadataContentRepository(
        jpa: MetadataContentJpaRepository
    ): MetadataContentRepository = JpaMetadataContentRepository(jpa)

    @Bean
    fun metadataSeedLedgerRepository(
        jpa: MetadataSeedLedgerJpaRepository
    ): MetadataSeedLedgerRepository = JpaMetadataSeedLedgerRepository(jpa)
}
