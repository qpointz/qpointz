package io.qpointz.mill.data.odata.service.config

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.data.odata.service.render.MillMetadataDocumentRenderer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.ComponentScan

/**
 * Registers the OData MVC controllers after [ODataWebAutoConfiguration] beans.
 */
@AutoConfiguration(after = [ODataWebAutoConfiguration::class])
@ConditionalOnService(value = "odata", group = "data")
@ComponentScan(
    basePackageClasses = [
        MillODataController::class,
        ODataCatalogController::class,
        MillMetadataDocumentRenderer::class,
    ],
)
class ODataMvcAutoConfiguration
