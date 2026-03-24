package io.qpointz.mill.data.backend.grpc

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.service.descriptors.ServiceDescriptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Advertises the gRPC data-plane endpoint to Mill service discovery.
 */
@Component
@ConditionalOnService(value = "grpc", group = "data")
class GrpcServiceDescriptor(
    @Value("\${mill.data.services.grpc.host.external:}") externalHostReference: String?,
) : ServiceDescriptor {

    /**
     * Optional external host reference (for proxies / service mesh), when configured.
     */
    data class HostDescriptor(
        val external: String,
    )

    val host: HostDescriptor? =
        if (externalHostReference.isNullOrBlank()) null else HostDescriptor(externalHostReference)

    override fun getStereotype(): String = "grpc"
}
