package io.qpointz.mill.data.query

import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Well-known advertisement for the query-result HTTP surface (`DescriptorTypes.SERVICE_TYPE_NAME`).
 *
 * Binds [QueryResultServiceProperties] for `mill.data.services.query.*`;
 * see [QueryResultConnectionDescriptor] for host/port/scheme in the
 * `DescriptorTypes.CONNECTIONS_TYPE_NAME` bucket.
 */
@Component
@ConditionalOnService(value = "query", group = "data")
@EnableConfigurationProperties(QueryResultServiceProperties::class)
class QueryResultServiceDescriptor : Descriptor {

    /**
     * Logical service id in discovery JSON (alongside `data-http`, `data-grpc`, `data-export`).
     */
    val name: String = "data-query"

    override fun getTypeName(): String = DescriptorTypes.SERVICE_TYPE_NAME
}
