package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.FacetRepository
import io.qpointz.mill.metadata.repository.FacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.FacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeDefinitionRepository
import io.qpointz.mill.metadata.repository.InMemoryFacetTypeRepository
import io.qpointz.mill.metadata.repository.InMemoryMetadataEntityRepository
import io.qpointz.mill.metadata.repository.InMemoryMetadataScopeRepository
import io.qpointz.mill.metadata.repository.MetadataEntityRepository
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
/**
 * In-memory repository beans plus path validation when `mill.metadata.repository.type=file`.
 *
 * <p>Populate repositories via `mill.metadata.seed.resources` (see [MetadataSeedStartup]) and/or
 * `mill.metadata.repository.file.path` when non-blank (at least one must be configured).
 */
@AutoConfiguration
@AutoConfigureAfter(MetadataCoreConfiguration::class)
@AutoConfigureBefore(MetadataRepositoryAutoConfiguration::class)
@EnableConfigurationProperties(MetadataProperties::class, MetadataSeedProperties::class)
@ConditionalOnProperty(
    prefix = "mill.metadata.repository",
    name = ["type"],
    havingValue = "file",
    matchIfMissing = true
)
class MetadataFileRepositoryAutoConfiguration {

    @Bean
    fun metadataFileRepositorySettingsValidator(
        properties: MetadataProperties,
        seedProperties: MetadataSeedProperties
    ): MetadataFileRepositorySettingsValidator =
        MetadataFileRepositorySettingsValidator(properties, seedProperties)

    @Bean
    fun fileBackedMetadataEntityRepository(): MetadataEntityRepository = InMemoryMetadataEntityRepository()

    @Bean
    fun fileBackedFacetRepository(): FacetRepository = InMemoryFacetRepository()

    @Bean
    fun fileBackedMetadataScopeRepository(): MetadataScopeRepository = InMemoryMetadataScopeRepository()

    @Bean
    fun fileBackedFacetTypeDefinitionRepository(): FacetTypeDefinitionRepository =
        InMemoryFacetTypeDefinitionRepository()

    @Bean
    fun fileBackedFacetTypeRepository(): FacetTypeRepository = InMemoryFacetTypeRepository()
}

/**
 * Fails fast when the file backend is active but neither [MetadataProperties.Repository.File.path]
 * nor [MetadataSeedProperties.getResources] supplies content sources.
 *
 * @param properties bound `mill.metadata` properties
 * @param seedProperties bound `mill.metadata.seed` properties
 */
class MetadataFileRepositorySettingsValidator(
    private val properties: MetadataProperties,
    private val seedProperties: MetadataSeedProperties
) {
    @PostConstruct
    fun validate() {
        val path = properties.repository.file.path
        val seeds = seedProperties.resources
        if (path.isNullOrBlank() && seeds.isEmpty()) {
            throw IllegalStateException(
                "mill.metadata.repository.type=file requires a non-blank mill.metadata.repository.file.path " +
                    "or a non-empty mill.metadata.seed.resources list"
            )
        }
        if (properties.repository.file.isWritable) {
            // SPEC §15.1 — write-back not implemented for the in-memory file bootstrap yet
        }
        if (properties.repository.file.isWatch) {
            // Hot reload not implemented
        }
    }
}
