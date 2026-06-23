package io.qpointz.mill.data.odata.service;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.Descriptor;
import io.qpointz.mill.service.descriptors.DescriptorTypes;
import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Well-known advertisement for the OData v4 HTTP surface.
 */
@Component
@ConditionalOnService(value = "odata", group = "data")
@EnableConfigurationProperties(ODataServiceProperties.class)
public class ODataServiceDescriptor implements Descriptor {

    /** Logical service id in discovery JSON. */
    @Getter
    private final String name = "data-odata";

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.SERVICE_TYPE_NAME;
    }
}
