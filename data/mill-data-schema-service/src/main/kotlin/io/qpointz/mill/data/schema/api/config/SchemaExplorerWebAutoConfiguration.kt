package io.qpointz.mill.data.schema.api.config

import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.api.SchemaExceptionHandler
import io.qpointz.mill.data.schema.api.SchemaExplorerController
import io.qpointz.mill.data.schema.api.SchemaExplorerService
import io.qpointz.mill.data.schema.api.SchemaExplorerServiceDescriptor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Import

/**
 * Registers schema explorer REST MVC when [SchemaFacetService] is available.
 *
 * Controllers and services in this module are excluded from the host application's global
 * component scan (see [io.qpointz.mill.app.MillService]) so startup does not fail when
 * metadata/schema infrastructure is absent.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(SchemaFacetService::class)
@Import(
    SchemaExplorerController::class,
    SchemaExplorerService::class,
    SchemaExceptionHandler::class,
    SchemaExplorerServiceDescriptor::class,
)
class SchemaExplorerWebAutoConfiguration
