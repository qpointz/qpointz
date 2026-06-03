package io.qpointz.mill.data.backend.grpc

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.data.backend.grpc.config.GrpcServerProperties
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import io.qpointz.mill.service.descriptors.ServiceAddressScheme
import io.qpointz.mill.service.providers.ExternalHostLookup
import io.qpointz.mill.service.providers.ExternalHostsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Locale

/**
 * Connection hints for the native gRPC data plane: scheme, host, and port for clients.
 *
 * Resolves [GrpcServerProperties.externalHost] against [ExternalHostsProvider.getExternals] on each access
 * (supports `@request.*` placeholders in config); otherwise falls back to localhost and configured listen port.
 *
 * @param serverProperties gRPC server and `external-host` bindings
 * @param externalHosts optional map of named external addresses from `mill.application.hosts.externals`
 */
@Component
@ConditionalOnService(value = "grpc", group = "data")
class GrpcConnectionDescriptor(
    private val serverProperties: GrpcServerProperties,
    @Autowired(required = false) private val externalHosts: ExternalHostsProvider?,
) : Descriptor {

    private val hostRef: String = serverProperties.externalHost

    /** Client port (from externals map when resolved, otherwise configured listen port). */
    val port: Int
        get() = resolvedExternal()?.port ?: serverProperties.port

    /** Client hostname for gRPC connections. */
    val host: String
        get() = resolvedExternal()?.host ?: "localhost"

    /** URL scheme for client connections (typically `grpc`). */
    val scheme: String
        get() = resolvedExternal()?.scheme?.toString()?.lowercase(Locale.getDefault())
            ?: ServiceAddressScheme.GRPC.name.lowercase(Locale.getDefault())

    private fun resolvedExternal() = ExternalHostLookup.resolve(externalHosts, hostRef)

    override fun getTypeName(): String = DescriptorTypes.CONNECTIONS_TYPE_NAME
}
