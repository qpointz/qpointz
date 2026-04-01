package io.qpointz.mill.autoconfigure.data.schema;

import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.schema.MetadataEntityUrnCodec;
import io.qpointz.mill.data.schema.SchemaFacetService;
import io.qpointz.mill.data.schema.SchemaFacetServiceImpl;
import io.qpointz.mill.metadata.repository.EntityReadSide;
import io.qpointz.mill.metadata.service.FacetCatalog;
import io.qpointz.mill.metadata.service.FacetInstanceReadMerge;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers {@link SchemaFacetService} (physical schema + merged facets) once {@link SchemaProvider}
 * and metadata read infrastructure exist.
 */
@AutoConfiguration
@AutoConfigureAfter(
        value = {BackendAutoConfiguration.class, LogicalLayoutMetadataSourceAutoConfiguration.class},
        name = {
                "io.qpointz.mill.metadata.configuration.MetadataRepositoryAutoConfiguration",
                "io.qpointz.mill.metadata.configuration.MetadataJpaPersistenceAutoConfiguration",
                "io.qpointz.mill.metadata.configuration.MetadataCoreConfiguration",
                "io.qpointz.mill.metadata.configuration.MetadataEntityServiceAutoConfiguration"
        })
@ConditionalOnClass(SchemaFacetService.class)
public class SchemaFacetServiceAutoConfiguration {

    /**
     * @param schemaProvider     physical schema source
     * @param entityRead         {@code metadata_entity} read side
     * @param facetReadMerge     layered facet read merge over all {@link io.qpointz.mill.metadata.source.MetadataSource} beans
     * @param facetCatalog       facet type definitions for schema shaping
     * @return schema explorer aggregate
     */
    @Bean
    @ConditionalOnMissingBean(SchemaFacetService.class)
    @ConditionalOnBean({
            SchemaProvider.class,
            EntityReadSide.class,
            FacetInstanceReadMerge.class,
            FacetCatalog.class,
            MetadataEntityUrnCodec.class
    })
    public SchemaFacetService schemaFacetService(
            SchemaProvider schemaProvider,
            EntityReadSide entityRead,
            FacetInstanceReadMerge facetReadMerge,
            FacetCatalog facetCatalog,
            MetadataEntityUrnCodec metadataEntityUrnCodec) {
        return new SchemaFacetServiceImpl(
                schemaProvider,
                entityRead,
                facetReadMerge,
                facetCatalog,
                metadataEntityUrnCodec
        );
    }
}
