package io.qpointz.mill.metadata.configuration

import io.qpointz.mill.metadata.repository.MetadataSeedLedgerRepository
import io.qpointz.mill.metadata.service.MetadataImportService
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader

/**
 * Ordered startup seeds: `mill.metadata.seed.resources` applied via [MetadataImportService] with ledger skips (SPEC §14.1).
 */
@AutoConfiguration
@AutoConfigureAfter(
    MetadataImportExportAutoConfiguration::class,
    MetadataEntityServiceAutoConfiguration::class
)
@EnableConfigurationProperties(MetadataSeedProperties::class)
class MetadataSeedAutoConfiguration {

    @Bean
    fun metadataSeedStartup(
        seedProperties: MetadataSeedProperties,
        importService: MetadataImportService,
        ledger: MetadataSeedLedgerRepository,
        resourceLoader: ResourceLoader
    ): MetadataSeedStartup = MetadataSeedStartup(seedProperties, importService, ledger, resourceLoader)

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 40)
    fun metadataSeedApplicationRunner(startup: MetadataSeedStartup): ApplicationRunner =
        ApplicationRunner { startup.run() }

}
