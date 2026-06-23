package io.qpointz.mill.data.odata.edm

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * OData naming conventions for per-schema services at {@code /services/odata/{schema}.svc}.
 */
object EntitySetNaming {
    const val MODEL_NAMESPACE_PREFIX = "Mill"

    /** Path segment between {@code /odata/} and {@code .svc} in the service root. */
    private val SERVICE_ROOT_SCHEMA =
        Regex("(?i)/odata/([^/?#]+)\\.svc/?")

    /**
     * @param schemaName physical schema name
     * @return EDM namespace for entity types in that schema
     */
    fun entityTypeNamespace(schemaName: String): String =
        "$MODEL_NAMESPACE_PREFIX.$schemaName"

    /**
     * @param schemaName physical schema name
     * @param tableName physical table name
     * @return fully-qualified entity type name
     */
    fun entityTypeFqn(schemaName: String, tableName: String): String =
        "${entityTypeNamespace(schemaName)}.$tableName"

    /**
     * Extracts the physical schema name from an absolute OData service root URL.
     *
     * @param serviceRoot absolute URL ending in {@code /odata/{schema}.svc}
     * @return decoded schema segment, or null when the URL does not match the convention
     */
    fun extractSchemaFromServiceRoot(serviceRoot: String): String? {
        val path = serviceRoot.substringBefore('?').substringBefore('#')
        val match = SERVICE_ROOT_SCHEMA.find(path) ?: return null
        val raw = match.groupValues[1]
        if (raw.isBlank() || raw.contains('/') || raw.contains("..")) {
            return null
        }
        return URLDecoder.decode(raw, StandardCharsets.UTF_8)
    }
}
