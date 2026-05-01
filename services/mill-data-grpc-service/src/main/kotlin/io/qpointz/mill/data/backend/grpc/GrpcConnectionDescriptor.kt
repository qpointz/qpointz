package io.qpointz.mill.data.backend.grpc

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.data.backend.grpc.config.GrpcServerProperties
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor
import io.qpointz.mill.service.descriptors.ServiceAddressScheme
import io.qpointz.mill.service.providers.ExternalHostsProvider
import lombok.Data
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Locale

/**
 * Connection hints for the native gRPC data plane: scheme, host, and port for clients.
 *
 * <p>Resolves {@link GrpcServerProperties#getExternalHost()} as a key into {@link ExternalHostsProvider#getExternals()}
 * when an {@link ExternalHostsProvider} bean is present (typically {@link io.qpointz.mill.service.configuration.ServiceAddressProperties});
 * otherwise falls back to localhost and configured listen port.
 *
 * @param serverProperties gRPC server and {@code external-host} bindings
 * @param externalHosts    optional map of named external addresses from {@code mill.application.hosts.externals}
 */
@Component
@Data
@ConditionalOnService(value = "grpc", group = "data")
class GrpcConnectionDescriptor(
    serverProperties: GrpcServerProperties,
    @Autowired(required = false) externalHosts: ExternalHostsProvider?,
) : Descriptor {

    private val hostRef: String = serverProperties.externalHost

    private val resolvedExternal: ServiceAddressDescriptor? =
        if (externalHosts != null && hostRef.isNotBlank()) {
            externalHosts.getExternals()[hostRef]
        } else {
            null
        }

    /** Client port (from externals map when resolved, otherwise configured listen port). */
    val port: Int = this.resolvedExternal?.port ?: serverProperties.port

    /** Client hostname for gRPC connections. */
    val host: String = this.resolvedExternal?.host ?: "localhost"

    /** URL scheme for client connections (typically {@code grpc}). */
    val scheme: String =
        this.resolvedExternal?.scheme?.toString()?.lowercase(Locale.getDefault())
            ?: ServiceAddressScheme.GRPC.name.lowercase(Locale.getDefault())

    /**
     * @return {@link DescriptorTypes#CONNECTIONS_TYPE_NAME}
     */
    override fun getTypeName(): String = DescriptorTypes.CONNECTIONS_TYPE_NAME
}
