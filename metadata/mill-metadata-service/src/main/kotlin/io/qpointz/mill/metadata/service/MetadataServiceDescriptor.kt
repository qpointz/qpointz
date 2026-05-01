package io.qpointz.mill.metadata.service

import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import org.springframework.stereotype.Component

/**
 * Well-known advertisement for the metadata REST API packaged in this module.
 */
@Component
class MetadataServiceDescriptor : Descriptor {

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    override fun getTypeName(): String = DescriptorTypes.SERVICE_TYPE_NAME

    /** Logical service id in discovery JSON. */
    val name: String = "api-metadata"
}
