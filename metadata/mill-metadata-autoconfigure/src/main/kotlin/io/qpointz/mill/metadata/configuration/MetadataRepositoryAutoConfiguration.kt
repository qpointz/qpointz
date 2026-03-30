package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.MetadataAuditRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository
import io.qpointz.mill.metadata.repository.NoOpFacetRepository
import io.qpointz.mill.metadata.repository.NoOpFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.NoOpFacetTypeRepository
import io.qpointz.mill.metadata.repository.NoOpMetadataAuditRepository
import io.qpointz.mill.metadata.repository.NoOpMetadataEntityRepository
import io.qpointz.mill.metadata.repository.NoOpMetadataScopeRepository
import io.qpointz.mill.metadata.repository.NoOpMetadataSeedLedgerRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Registers no-op repository fallbacks when JPA beans are absent, so optional metadata consumers
 * (for example [io.qpointz.mill.data.schema.SchemaFacetService]) still receive injectable
 * collaborators.
 *
 * When `mill.metadata.repository.type=jpa`, [MetadataJpaPersistenceAutoConfiguration] runs first
 * (via [@AutoConfigureAfter]) and supplies real repositories, so these beans stay inactive.
 */
@AutoConfiguration
@AutoConfigureAfter(MetadataJpaPersistenceAutoConfiguration::class)
@EnableConfigurationProperties(MetadataProperties::class)
class MetadataRepositoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MetadataEntityRepository::class)
    fun noOpMetadataEntityRepository(): MetadataEntityRepository {
        log.info("No MetadataEntityRepository bean — using NoOpMetadataEntityRepository")
        return NoOpMetadataEntityRepository()
    }

    @Bean
    @ConditionalOnMissingBean(FacetRepository::class)
    fun noOpFacetRepository(): FacetRepository {
        log.info("No FacetRepository bean — using NoOpFacetRepository")
        return NoOpFacetRepository()
    }

    @Bean
    @ConditionalOnMissingBean(MetadataScopeRepository::class)
    fun noOpMetadataScopeRepository(): MetadataScopeRepository {
        log.info("No MetadataScopeRepository bean — using NoOpMetadataScopeRepository")
        return NoOpMetadataScopeRepository()
    }

    @Bean
    @ConditionalOnMissingBean(FacetTypeDefinitionRepository::class)
    fun noOpFacetTypeDefinitionRepository(): FacetTypeDefinitionRepository {
        log.info("No FacetTypeDefinitionRepository bean — using NoOpFacetTypeDefinitionRepository")
        return NoOpFacetTypeDefinitionRepository()
    }

    @Bean
    @ConditionalOnMissingBean(FacetTypeRepository::class)
    fun noOpFacetTypeRepository(): FacetTypeRepository {
        log.info("No FacetTypeRepository bean — using NoOpFacetTypeRepository")
        return NoOpFacetTypeRepository()
    }

    @Bean
    @ConditionalOnMissingBean(MetadataAuditRepository::class)
    fun noOpMetadataAuditRepository(): MetadataAuditRepository {
        log.info("No MetadataAuditRepository bean — using NoOpMetadataAuditRepository")
        return NoOpMetadataAuditRepository()
    }

    @Bean
    @ConditionalOnMissingBean(MetadataSeedLedgerRepository::class)
    fun noOpMetadataSeedLedgerRepository(): MetadataSeedLedgerRepository {
        log.info("No MetadataSeedLedgerRepository bean — using NoOpMetadataSeedLedgerRepository")
        return NoOpMetadataSeedLedgerRepository()
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataRepositoryAutoConfiguration::class.java)
    }
}
