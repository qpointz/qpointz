package io.qpointz.mill.service.descriptors;

/**
 * One supported authentication mechanism exposed in the well-known security block.
 *
 * @param authMethod enumeration value describing the mechanism
 */
public record AuthMethodDescriptor(AuthMethod authMethod) implements Descriptor {

    /**
     * @return {@link DescriptorTypes#AUTH_METHODS_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.AUTH_METHODS_TYPE_NAME;
    }

}
