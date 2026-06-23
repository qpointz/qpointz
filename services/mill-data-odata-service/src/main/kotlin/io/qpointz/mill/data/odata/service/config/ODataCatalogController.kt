package io.qpointz.mill.data.odata.service.config

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.odata.service.ODataBaseUrlResolver
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

/**
 * OData schema discovery under {@code /services/odata/schemas}.
 */
@RestController
@RequestMapping("/services/odata")
@ConditionalOnService(value = "odata", group = "data")
class ODataCatalogController(
    private val schemaProvider: SchemaProvider,
    private val baseUrlResolver: ODataBaseUrlResolver,
) {

    /**
     * @param request inbound servlet request for origin resolution
     * @return physical schemas with per-schema OData service root URLs
     */
    @GetMapping("/schemas")
    fun listSchemas(request: HttpServletRequest): List<Map<String, Any>> {
        val origin = baseUrlResolver.origin(request)
        return schemaProvider.schemaNames.map { name ->
            val schema = schemaProvider.getSchema(name)
            linkedMapOf(
                "name" to name,
                "href" to "$origin/services/odata/${encPath(name)}.svc/",
                "tableCount" to schema.tablesCount,
            )
        }
    }

    private fun encPath(segment: String): String =
        UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8)
}
