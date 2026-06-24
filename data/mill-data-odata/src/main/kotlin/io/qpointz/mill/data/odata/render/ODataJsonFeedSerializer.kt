package io.qpointz.mill.data.odata.render

import tools.jackson.databind.json.JsonMapper
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Serializes query row maps as an OData v4 JSON feed for RWS [com.sdl.odata.api.processor.query.QueryResult] RAW_JSON rendering.
 *
 * <p>RWS JSON renderers resolve entity types from Java classes; Mill returns {@code Map} rows from the
 * dispatcher, so the feed is pre-serialized and passed through as raw JSON.
 */
class ODataJsonFeedSerializer @JvmOverloads constructor(
    private val mapper: JsonMapper = JsonMapper.builder().findAndAddModules().build(),
) {

    /**
     * @param rows materialized entity rows keyed by EDM property name
     * @param entitySetName OData entity set name (physical table name, e.g. {@code cities})
     * @param serviceRoot absolute OData service root ending in {@code /odata/{schema}.svc}
     * @return OData JSON object with {@code @odata.context} and {@code value}
     */
    fun serializeFeed(
        rows: List<Map<String, Any?>>,
        entitySetName: String,
        serviceRoot: String,
    ): String {
        val normalizedRoot = serviceRoot.trimEnd('/')
        val payload = linkedMapOf<String, Any>(
            "@odata.context" to "$normalizedRoot/\$metadata#$entitySetName",
            "value" to rows.map { row -> row.mapValues { (_, value) -> toODataJsonValue(value) } },
        )
        return mapper.writeValueAsString(payload)
    }

    /**
     * SQL {@code DATE} values are exposed as {@code Edm.DateTimeOffset} for BI clients; serialize at UTC midnight.
     */
    private fun toODataJsonValue(value: Any?): Any? =
        when (value) {
            null -> null
            is Date -> toUtcMidnightOffset(value.toLocalDate())
            is LocalDate -> toUtcMidnightOffset(value)
            else -> value
        }

    private fun toUtcMidnightOffset(localDate: LocalDate): String =
        localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toString()
}
