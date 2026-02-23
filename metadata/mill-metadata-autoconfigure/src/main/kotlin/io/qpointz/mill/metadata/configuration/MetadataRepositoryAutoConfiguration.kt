package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.MetadataRepository
import io.qpointz.mill.metadata.repository.file.FileMetadataRepository
import io.qpointz.mill.metadata.repository.file.SpringResourceResolver
import io.qpointz.mill.metadata.service.FacetCatalog
import io.qpointz.mill.metadata.service.MetadataService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader

/** Auto-configures file-backed repository and [MetadataService] wiring. */
@AutoConfiguration
@EnableConfigurationProperties(MetadataProperties::class)
@ConditionalOnMissingBean(MetadataRepository::class)
@ConditionalOnProperty(
    prefix = "mill.metadata.v2.storage",
    name = ["type"],
    havingValue = "file",
    matchIfMissing = true
)
open class MetadataRepositoryAutoConfiguration {

    @Bean
    open fun fileMetadataRepository(
        resourceLoader: ResourceLoader,
        properties: MetadataProperties
    ): MetadataRepository {
        val locations = properties.file.path.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        log.info("Creating FileMetadataRepository with {} location(s): {}", locations.size, locations)
        return FileMetadataRepository(locations, SpringResourceResolver(resourceLoader))
    }

    @Bean
    @ConditionalOnBean(MetadataRepository::class)
    open fun metadataService(
        repository: MetadataRepository,
        @Autowired(required = false) facetCatalog: FacetCatalog?
    ): MetadataService = MetadataService(repository, facetCatalog)

    companion object {
        private val log = LoggerFactory.getLogger(MetadataRepositoryAutoConfiguration::class.java)
    }
}
