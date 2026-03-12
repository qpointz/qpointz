package io.qpointz.mill.autoconfigure.data.schema;

import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.schema.SchemaFacetService;
import io.qpointz.mill.data.schema.SchemaFacetServiceImpl;
import io.qpointz.mill.metadata.repository.MetadataRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configures {@link SchemaFacetService} when both a {@link SchemaProvider} and a
 * {@link MetadataRepository} bean are present on the application context.
 */
@AutoConfiguration
@ConditionalOnBean({SchemaProvider.class, MetadataRepository.class})
public class SchemaFacetAutoConfiguration {

    /**
     * Creates a {@link SchemaFacetServiceImpl} wired with the application-context
     * {@link SchemaProvider} and {@link MetadataRepository} beans, using the default
     * {@code "global"} facet scope.
     * Skipped when a {@link SchemaFacetService} bean is already registered.
     */
    @Bean
    @ConditionalOnMissingBean
    public SchemaFacetService schemaFacetService(
            SchemaProvider schemaProvider,
            MetadataRepository metadataRepository) {
        return new SchemaFacetServiceImpl(schemaProvider, metadataRepository);
    }
}
