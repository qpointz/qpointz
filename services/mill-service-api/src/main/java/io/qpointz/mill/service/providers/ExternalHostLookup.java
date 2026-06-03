package io.qpointz.mill.service.providers;

import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;

/**
 * Resolves a logical {@code external-host} key against {@link ExternalHostsProvider#getExternals()}.
 */
public final class ExternalHostLookup {

    private ExternalHostLookup() {
    }

    /**
     * @param provider   optional external hosts provider
     * @param hostRef    logical key from service configuration (e.g. {@code grpc-request})
     * @return resolved descriptor, or {@code null} when provider or key is absent
     */
    public static ServiceAddressDescriptor resolve(ExternalHostsProvider provider, String hostRef) {
        if (provider == null || hostRef == null || hostRef.isBlank()) {
            return null;
        }
        return provider.getExternals().get(hostRef);
    }
}
