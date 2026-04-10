package io.qpointz.mill.autoconfigure.data.schema;

import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.metadata.source.LogicalLayoutMetadataSource;
import io.qpointz.mill.metadata.source.MetadataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers the {@link SchemaProvider}-backed {@link MetadataSource} that contributes inferred layout
 * facets. This lives in the data autoconfigure module; {@code mill-metadata-autoconfigure} only
 * registers persistence-backed sources and aggregates every {@link MetadataSource} bean into
 * {@link io.qpointz.mill.metadata.service.FacetInstanceReadMerge}.
 */
@AutoConfiguration
@AutoConfigureAfter(BackendAutoConfiguration.class)
@ConditionalOnClass(LogicalLayoutMetadataSource.class)
public class LogicalLayoutMetadataSourceAutoConfiguration {

    /**
     * @param schemaProvider physical schema snapshot used for inferred structural/descriptive rows
     * @return logical-layout contributor (origin {@link io.qpointz.mill.metadata.source.MetadataOriginIds#LOGICAL_LAYOUT})
     */
    @Bean
    @ConditionalOnBean(SchemaProvider.class)
    @ConditionalOnMissingBean(LogicalLayoutMetadataSource.class)
    public LogicalLayoutMetadataSource logicalLayoutMetadataSource(SchemaProvider schemaProvider) {
        return new LogicalLayoutMetadataSource(schemaProvider);
    }
}
