package io.qpointz.mill.data.odata.expr

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

/**
 * Rewrites DateTimeOffset-style literals in OData `$filter` expressions to [Edm.Date] literals.
 *
 * <p>Power BI incremental refresh and range parameters often send values such as
 * {@code 2026-03-30T22:00:00.000Z} while Mill exposes SQL {@code DATE} columns as {@code Edm.Date}.
 * The RWS parser rejects those literals before query translation; this rewriter normalizes them
 * to calendar dates using the instant's offset (UTC for {@code Z} suffixes).
 */
object ODataFilterDateLiteralRewriter {

    private val DATETIME_OFFSET_QUOTED =
        Regex("""datetimeoffset'([^']+)'""", RegexOption.IGNORE_CASE)

    private val DATETIME_QUOTED =
        Regex("""datetime'([^']+)'""", RegexOption.IGNORE_CASE)

    private val SINGLE_QUOTED_ISO_DATETIME =
        Regex(
            """'(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2}(?::\d{2})?))'""",
        )

    private val BARE_ISO_DATETIME =
        Regex(
            """(?<![\w'"./])(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?)(Z|[+-]\d{2}:\d{2}(?::\d{2})?)""",
        )

    /**
     * @param requestUri absolute OData request URI, including an optional query string
     * @return the same URI with a rewritten `$filter` parameter when present
     */
    fun rewriteRequestUri(requestUri: String): String {
        val queryStart = requestUri.indexOf('?')
        if (queryStart < 0) {
            return requestUri
        }
        val base = requestUri.substring(0, queryStart)
        val query = requestUri.substring(queryStart + 1)
        val rewritten = rewriteQueryString(query)
        return if (rewritten == query) {
            requestUri
        } else {
            "$base?$rewritten"
        }
    }

    /**
     * @param filter OData `$filter` expression (decoded)
     * @return expression with datetime literals replaced by Edm.Date literals
     */
    fun rewriteFilterExpression(filter: String): String {
        var rewritten = DATETIME_OFFSET_QUOTED.replace(filter) { match ->
            toDateLiteral(match.groupValues[1])
        }
        rewritten = DATETIME_QUOTED.replace(rewritten) { match ->
            toDateLiteral(match.groupValues[1])
        }
        rewritten = SINGLE_QUOTED_ISO_DATETIME.replace(rewritten) { match ->
            toDateLiteral(match.groupValues[1])
        }
        rewritten = BARE_ISO_DATETIME.replace(rewritten) { match ->
            toDateLiteral(match.groupValues[1] + match.groupValues[2])
        }
        return rewritten
    }

    private fun rewriteQueryString(query: String): String =
        query.split('&')
            .joinToString("&") { part ->
                val separator = part.indexOf('=')
                if (separator < 0) {
                    return@joinToString part
                }
                val encodedName = part.substring(0, separator)
                val encodedValue = part.substring(separator + 1)
                val name = URLDecoder.decode(encodedName, StandardCharsets.UTF_8)
                if (!isFilterParameter(name)) {
                    part
                } else {
                    val decoded = URLDecoder.decode(encodedValue, StandardCharsets.UTF_8)
                    val rewritten = rewriteFilterExpression(decoded)
                    if (rewritten == decoded) {
                        part
                    } else {
                        "${encodeQueryComponent(name)}=${encodeQueryComponent(rewritten)}"
                    }
                }
            }

    private fun isFilterParameter(name: String): Boolean =
        name.equals("\$filter", ignoreCase = true)

    private fun toDateLiteral(isoDateTime: String): String =
        OffsetDateTime.parse(isoDateTime).toLocalDate().toString()

    private fun encodeQueryComponent(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)
}
