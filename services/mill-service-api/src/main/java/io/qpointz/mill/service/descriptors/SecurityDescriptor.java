package io.qpointz.mill.service.descriptors;

import java.util.Collection;

/**
 * High-level security posture: whether auth is enforced and which methods are advertised.
 *
 * @param enabled     {@code true} when Mill security is active for this deployment
 * @param authMethods supported authentication methods; empty when security is disabled or none configured
 */
public record SecurityDescriptor(boolean enabled, Collection<AuthMethodDescriptor> authMethods) implements Descriptor {

    /**
     * @return {@link DescriptorTypes#SECURITY_TYPE_NAME}
     */
    @Override
    public String getTypeName() {
        return DescriptorTypes.SECURITY_TYPE_NAME;
    }

}
