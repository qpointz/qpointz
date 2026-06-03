package io.qpointz.mill.service.providers;

import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;

import java.util.Map;

/**
 * Resolved view of named external service addresses for discovery and absolute URL building.
 *
 * <p>Entries come from {@code mill.application.hosts.externals}, with optional per-field
 * {@link ServiceAddressPlaceholders} (e.g. {@code host: "@request.host"}) resolved on each call when a
 * servlet request is active.
 */
public interface ExternalHostsProvider {

    /**
     * @return map of logical keys to resolved descriptors; never {@code null}; entries using request
     *         placeholders are omitted when no request is bound
     */
    Map<String, ServiceAddressDescriptor> getExternals();
}
