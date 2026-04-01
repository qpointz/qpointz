package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.service.DefaultFacetCatalog
import io.qpointz.mill.metadata.service.FacetCatalog
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Core metadata beans: facet catalog and registry guards.
 *
 * **Seeding:** platform scopes, facet types, and entities are applied **only** through
 * `mill.metadata.seed.resources` ([MetadataSeedStartup]); there are no Flyway data inserts or
 * autoconfigure `ApplicationRunner` seeders for facet definitions.
 */
@Configuration
@EnableConfigurationProperties(MetadataProperties::class)
open class MetadataCoreConfiguration {

    /**
     * In-memory facet type definitions when JPA metadata storage is not active.
     *
     * <p>Skipped when {@code mill.metadata.repository.type=jpa} (JPA adapters) or {@code file}
     * ([MetadataFileRepositoryAutoConfiguration] supplies in-memory file-backed repos).
     */
    @Bean
    @ConditionalOnMissingBean(FacetTypeDefinitionRepository::class)
    @ConditionalOnExpression(
        "'\${mill.metadata.repository.type:file}' != 'jpa' && '\${mill.metadata.repository.type:file}' != 'file' " +
            "&& '\${mill.metadata.facet-type-registry.type:inMemory}' == 'inMemory'"
    )
    open fun inMemoryFacetTypeDefinitionRepository(): FacetTypeDefinitionRepository =
        InMemoryFacetTypeDefinitionRepository()

    /**
     * In-memory runtime facet type rows paired with [inMemoryFacetTypeDefinitionRepository].
     */
    @Bean
    @ConditionalOnMissingBean(FacetTypeRepository::class)
    @ConditionalOnExpression(
        "'\${mill.metadata.repository.type:file}' != 'jpa' && '\${mill.metadata.repository.type:file}' != 'file' " +
            "&& '\${mill.metadata.facet-type-registry.type:inMemory}' == 'inMemory'"
    )
    open fun inMemoryFacetTypeRepository(): FacetTypeRepository = InMemoryFacetTypeRepository()

    /**
     * Creates the default [FacetCatalog] backed by definition and runtime type repositories.
     *
     * @param definitionRepository persisted or in-memory facet type definitions
     * @param facetTypeRepository runtime facet type rows
     * @return configured catalog instance
     */
    @Bean
    @ConditionalOnMissingBean(FacetCatalog::class)
    open fun facetCatalog(
        definitionRepository: FacetTypeDefinitionRepository,
        facetTypeRepository: FacetTypeRepository
    ): FacetCatalog {
        log.info("Creating DefaultFacetCatalog")
        return DefaultFacetCatalog(definitionRepository, facetTypeRepository)
    }

    /**
     * Validates facet type registry strategy selection at startup.
     *
     * `portal` is declared as a contract for future work and intentionally fails fast.
     * `local` expects local persistence-backed descriptor sourcing (typically JPA).
     */
    @Bean
    open fun facetTypeRegistryStrategyGuard(
        properties: MetadataProperties
    ): ApplicationRunner = ApplicationRunner {
        val strategy = properties.facetTypeRegistry.type
        when (strategy) {
            "portal" -> throw IllegalStateException(
                "mill.metadata.facet-type-registry.type=portal is not implemented yet"
            )
            "local" -> {
                if (properties.repository.type != "jpa") {
                    throw IllegalStateException(
                        "mill.metadata.facet-type-registry.type=local requires mill.metadata.repository.type=jpa"
                    )
                }
            }
            "inMemory" -> {
                // default fallback strategy
            }
            else -> throw IllegalStateException(
                "Unsupported mill.metadata.facet-type-registry.type='$strategy'. Supported: inMemory, local, portal"
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetadataCoreConfiguration::class.java)
    }
}
