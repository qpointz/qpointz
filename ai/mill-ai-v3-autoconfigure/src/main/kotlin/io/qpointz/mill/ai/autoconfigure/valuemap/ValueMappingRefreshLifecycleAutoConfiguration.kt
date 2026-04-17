package io.qpointz.mill.ai.autoconfigure.valuemap

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.ai.autoconfigure.config.ValueMappingConfigurationProperties
import io.qpointz.mill.ai.autoconfigure.embedding.EmbeddingAutoConfiguration
import io.qpointz.mill.ai.data.valuemap.DataOperationColumnDistinctValueLoader
import io.qpointz.mill.ai.valuemap.ColumnDistinctValueLoader
import io.qpointz.mill.ai.valuemap.ValueMappingIndexingFacetTypes
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingIndexedAttributeDiscovery
import io.qpointz.mill.ai.valuemap.refresh.ValueMappingRefreshConfigurationBridge
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.persistence.metadata.jpa.repositories.MetadataFacetJpaRepository
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

/**
 * Prerequisite beans for value-mapping refresh ([ValueMappingIndexedAttributeDiscovery], [ColumnDistinctValueLoader], config bridge).
 *
 * [ValueMappingRefreshOrchestrator] and listeners live in [ValueMappingRefreshOrchestratorAutoConfiguration] so
 * `@ConditionalOnBean` on the orchestrator runs after these beans are registered (see that class KDoc).
 */
@ConditionalOnAiEnabled
@AutoConfiguration(
    after = [
        EmbeddingAutoConfiguration::class,
        ValueMappingSyncAutoConfiguration::class,
    ],
)
@AutoConfigureAfter(
    EmbeddingAutoConfiguration::class,
    ValueMappingSyncAutoConfiguration::class,
)
class ValueMappingRefreshLifecycleAutoConfiguration {

    @Bean
    @ConditionalOnBean(MetadataFacetJpaRepository::class)
    fun valueMappingIndexedAttributeDiscovery(
        repo: MetadataFacetJpaRepository,
    ): ValueMappingIndexedAttributeDiscovery =
        ValueMappingIndexedAttributeDiscovery {
            repo.listDistinctEntityResByFacetTypeRes(ValueMappingIndexingFacetTypes.AI_COLUMN_VALUE_MAPPING)
        }

    @Bean
    fun valueMappingRefreshConfigurationBridge(
        vm: ValueMappingConfigurationProperties,
    ): ValueMappingRefreshConfigurationBridge =
        object : ValueMappingRefreshConfigurationBridge {
            override val refreshStartupEnabled: Boolean get() = vm.refresh.onStartup.isEnabled
            override val refreshScheduledDisabled: Boolean get() = !vm.refresh.schedule.isEnabled
        }

    @Bean
    @ConditionalOnMissingBean(ColumnDistinctValueLoader::class)
    fun columnDistinctValueLoader(
        dispatcher: ObjectProvider<DataOperationDispatcher>,
        sqlDialectSpec: SqlDialectSpec
    ): ColumnDistinctValueLoader {
        val d = dispatcher.ifAvailable
        return if (d != null) {
            DataOperationColumnDistinctValueLoader(d, sqlDialectSpec)
        } else {
            ColumnDistinctValueLoader { _, _, _, _ -> emptyList() }
        }
    }
}
