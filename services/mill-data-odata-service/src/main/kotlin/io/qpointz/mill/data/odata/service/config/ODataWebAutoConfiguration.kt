package io.qpointz.mill.data.odata.service.config

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.autoconfigure.data.odata.ODataEngineAutoConfiguration
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.odata.edm.EntityDataModelFactory
import io.qpointz.mill.data.odata.exec.ODataQueryExecutor
import io.qpointz.mill.data.odata.service.ODataBaseUrlResolver
import io.qpointz.mill.data.odata.service.ODataServiceProperties
import io.qpointz.mill.data.odata.service.datasource.MillODataDataSourceProvider
import io.qpointz.mill.data.odata.resolve.EdmPropertyResolver
import io.qpointz.mill.data.odata.service.edm.ODataEdmCache
import io.qpointz.mill.data.odata.service.edm.ODataEdmRegistryCache
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.service.providers.ExternalHostsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary

/**
 * Registers OData MVC controller at `/services/odata/{schema}.svc` with Mill EDM and data source.
 */
@AutoConfiguration(after = [ODataEngineAutoConfiguration::class])
@ConditionalOnService(value = "odata", group = "data")
@ConditionalOnBean(EntityDataModelFactory::class)
@Import(MillODataRwsConfiguration::class)
@EnableConfigurationProperties(ODataServiceProperties::class)
class ODataWebAutoConfiguration {

    /**
     * @param serviceProperties OData service configuration
     * @param externalHosts optional external host map for absolute URLs
     * @return public base URL resolver for OData discovery links
     */
    @Bean
    @ConditionalOnMissingBean
    fun oDataBaseUrlResolver(
        serviceProperties: ODataServiceProperties,
        @Autowired(required = false) externalHosts: ExternalHostsProvider?,
    ): ODataBaseUrlResolver = ODataBaseUrlResolver(serviceProperties, externalHosts)

    /**
     * @param serviceProperties OData service configuration including cache toggles
     * @return Caffeine cache for EDM documents and table facet metadata
     */
    @Bean
    @ConditionalOnMissingBean
    fun oDataEdmCache(serviceProperties: ODataServiceProperties): ODataEdmCache {
        val edm = serviceProperties.cache.getEdm()
        return ODataEdmCache(edm.isEnabled, edm.getTtl())
    }

    /**
     * @param factory Mill schema-backed EDM builder
     * @param edmCache Caffeine-backed EDM cache
     * @return lazy per-schema EDM registry cache
     */
    @Bean
    @ConditionalOnMissingBean
    fun oDataEdmRegistryCache(
        factory: EntityDataModelFactory,
        edmCache: ODataEdmCache,
    ): ODataEdmRegistryCache =
        ODataEdmRegistryCache(factory, edmCache)

    /**
     * @param edmRegistryCache per-schema EDM registry and annotation provider
     * @return facet-derived CSDL annotations for {@code $metadata}
     */
    @Bean
    @ConditionalOnMissingBean
    fun edmAnnotationProvider(edmRegistryCache: ODataEdmRegistryCache): io.qpointz.mill.data.odata.annotation.EdmAnnotationProvider =
        edmRegistryCache

    /**
     * @param schemaFacetService merged schema and facet metadata
     * @param edmCache optional table metadata cache (same toggles as EDM cache)
     * @return property resolver with facet caching when enabled
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = ["odataCachingEdmPropertyResolver"])
    fun odataCachingEdmPropertyResolver(
        schemaFacetService: SchemaFacetService,
        edmCache: ODataEdmCache,
    ): EdmPropertyResolver =
        EdmPropertyResolver(schemaFacetService, edmCache.schemaTableCache())

    /**
     * @param queryExecutor Mill Rel to Substrait query path
     * @param serviceProperties OData service limits
     * @return RWS data source provider with push-down reads
     */
    @Bean
    @ConditionalOnMissingBean
    fun millODataDataSourceProvider(
        queryExecutor: ODataQueryExecutor,
        serviceProperties: ODataServiceProperties,
    ): MillODataDataSourceProvider =
        MillODataDataSourceProvider(queryExecutor, serviceProperties)

    /**
     * @param edmRegistryCache per-schema EDM registry cache
     * @param schemaProvider physical catalog for schema validation
     * @param oDataParser RWS URI parser
     * @param queryProcessor RWS query processor
     * @param rendererFactory RWS response renderer factory
     * @return synchronous OData request handler
     */
    @Bean
    @ConditionalOnMissingBean
    fun millODataSyncService(
        edmRegistryCache: ODataEdmRegistryCache,
        schemaProvider: SchemaProvider,
        oDataParser: com.sdl.odata.api.parser.ODataParser,
        queryProcessor: com.sdl.odata.api.processor.ODataQueryProcessor,
        rendererFactory: com.sdl.odata.api.renderer.RendererFactory,
    ): MillODataSyncService =
        MillODataSyncService(
            edmRegistryCache,
            schemaProvider,
            oDataParser,
            queryProcessor,
            rendererFactory,
        )

    // Controller is registered via ODataMvcAutoConfiguration component scan.
}
