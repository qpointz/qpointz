package io.qpointz.mill.service.configuration;

import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import io.qpointz.mill.service.providers.ExternalHostsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resolves {@code mill.application.hosts.externals} via {@link ServiceAddressPlaceholderResolver}
 * and exposes the result as {@link ExternalHostsProvider}.
 */
@Component
public class DefaultExternalHostProvider implements ExternalHostsProvider {

    private final ServiceAddressProperties properties;
    private final ServiceAddressPlaceholderResolver resolver;

    /**
     * @param properties optional {@link ServiceAddressProperties} from {@code mill.application.hosts}
     * @param resolver   placeholder resolver for {@code @request.*} fields
     */
    public DefaultExternalHostProvider(
            @Autowired(required = false) ServiceAddressProperties properties,
            ServiceAddressPlaceholderResolver resolver
    ) {
        this.properties = properties;
        this.resolver = resolver;
    }

    @Override
    public Map<String, ServiceAddressDescriptor> getExternals() {
        if (properties == null || properties.getExternals().isEmpty()) {
            return Map.of();
        }

        Map<String, ServiceAddressDescriptor> resolved = new LinkedHashMap<>();
        for (var entry : properties.getExternals().entrySet()) {
            resolver.resolve(entry.getValue()).ifPresent(descriptor -> resolved.put(entry.getKey(), descriptor));
        }
        return Map.copyOf(resolved);
    }
}
