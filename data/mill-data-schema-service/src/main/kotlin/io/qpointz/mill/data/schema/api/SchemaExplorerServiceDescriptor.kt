package io.qpointz.mill.data.schema.api

import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import org.springframework.stereotype.Component

/**
 * Well-known advertisement for the physical schema explorer REST API ({@code /api/v1/schema}).
 */
@Component
class SchemaExplorerServiceDescriptor : Descriptor {

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    override fun getTypeName(): String = DescriptorTypes.SERVICE_TYPE_NAME

    /** Logical service id in discovery JSON. */
    val name: String = "api-schema"
}
