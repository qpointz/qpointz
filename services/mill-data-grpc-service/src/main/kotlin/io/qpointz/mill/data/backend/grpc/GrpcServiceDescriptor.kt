package io.qpointz.mill.data.backend.grpc

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import org.springframework.stereotype.Component

/**
 * Advertises the gRPC data-plane service in the well-known discovery map under
 * {@link DescriptorTypes#SERVICE_TYPE_NAME}. Connection endpoints are published separately by
 * {@link GrpcConnectionDescriptor} (including {@code mill.data.services.grpc.external-host} resolution).
 */
@Component
@ConditionalOnService(value = "grpc", group = "data")
class GrpcServiceDescriptor : Descriptor {

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    override fun getTypeName(): String = DescriptorTypes.SERVICE_TYPE_NAME

    /** Stable logical name for this data-plane gRPC surface in discovery JSON. */
    val name: String = "data-grpc"
}
