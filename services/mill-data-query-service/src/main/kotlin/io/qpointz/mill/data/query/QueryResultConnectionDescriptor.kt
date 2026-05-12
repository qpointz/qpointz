package io.qpointz.mill.data.query

import com.fasterxml.jackson.annotation.JsonProperty
import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import io.qpointz.mill.service.descriptors.ServiceAddressScheme
import io.qpointz.mill.service.providers.ExternalHostsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Query-result HTTP connection hints for well-known discovery (scheme, host, port, API path).
 *
 * When [QueryResultServiceProperties.getExternalHost] is non-blank and an [ExternalHostsProvider]
 * is present, host/port/scheme are taken from `mill.application.hosts.externals.<key>`; otherwise defaults
 * match local servlet HTTP.
 */
@Component
@ConditionalOnService(value = "query", group = "data")
class QueryResultConnectionDescriptor(
    private val serviceProperties: QueryResultServiceProperties,
    @param:Autowired(required = false) private val externalHosts: ExternalHostsProvider?,
) : Descriptor {

    private data class Resolved(val scheme: String, val host: String, val port: Int)

    private val resolved: Resolved = resolve(serviceProperties, externalHosts)

    /** Wire scheme for query-result requests (e.g. `http`). */
    val scheme: String get() = resolved.scheme

    /** Hostname clients should use for query-result HTTP. */
    val host: String get() = resolved.host

    /** TCP port for query-result HTTP. */
    val port: Int get() = resolved.port

    /** Base path prefix for query-result REST endpoints (`POST /api/v1/query`, `GET …/rows`, etc.). */
    @get:JsonProperty("api-path")
    val path: String = "/api/v1/query/"

    override fun getTypeName(): String = DescriptorTypes.CONNECTIONS_TYPE_NAME

    private companion object {

        fun resolve(
            serviceProperties: QueryResultServiceProperties,
            externalHosts: ExternalHostsProvider?,
        ): Resolved {
            val hostRef = serviceProperties.externalHost
            val external = if (externalHosts != null && hostRef.isNotBlank()) {
                externalHosts.externals[hostRef]
            } else {
                null
            }
            return if (external != null) {
                Resolved(
                    scheme = external.scheme.toString().lowercase(),
                    host = external.host,
                    port = external.port ?: 8080,
                )
            } else {
                Resolved(
                    scheme = ServiceAddressScheme.HTTP.toString().lowercase(),
                    host = "localhost",
                    port = 8080,
                )
            }
        }
    }
}
