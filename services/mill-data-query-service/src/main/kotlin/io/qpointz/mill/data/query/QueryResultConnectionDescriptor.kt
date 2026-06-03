package io.qpointz.mill.data.query

import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import io.qpointz.mill.service.descriptors.ServiceAddressScheme
import io.qpointz.mill.service.providers.ExternalHostLookup
import io.qpointz.mill.service.providers.ExternalHostsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Query-result HTTP connection hints for well-known discovery (scheme, host, port, API path).
 *
 * When [QueryResultServiceProperties.externalHost] is non-blank and an [ExternalHostsProvider] is present,
 * host/port/scheme are taken from `mill.application.hosts.externals.<key>` on each access (supports
 * `@request.*` placeholders); otherwise defaults match local servlet HTTP.
 */
@Component
@ConditionalOnService(value = "query", group = "data")
class QueryResultConnectionDescriptor(
    private val serviceProperties: QueryResultServiceProperties,
    @param:Autowired(required = false) private val externalHosts: ExternalHostsProvider?,
) : Descriptor {

    private val hostRef: String = serviceProperties.externalHost

    /** Wire scheme for query-result requests (e.g. `http`). */
    val scheme: String
        get() = resolvedExternal()?.scheme?.toString()?.lowercase()
            ?: ServiceAddressScheme.HTTP.toString().lowercase()

    /** Hostname clients should use for query-result HTTP. */
    val host: String
        get() = resolvedExternal()?.host ?: "localhost"

    /** TCP port for query-result HTTP. */
    val port: Int
        get() = resolvedExternal()?.port ?: 8080

    /** Base path prefix for query-result REST endpoints (`POST /api/v1/query`, `GET /api/v1/query/{id}`, etc.). */
    @get:JsonProperty("api-path")
    val path: String = "/api/v1/query/"

    private fun resolvedExternal() = ExternalHostLookup.resolve(externalHosts, hostRef)

    override fun getTypeName(): String = DescriptorTypes.CONNECTIONS_TYPE_NAME
}
