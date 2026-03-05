package io.qpointz.mill.service.providers;

import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;

import java.util.Map;

public interface ExternalHostsProvider {
    Map<String, ServiceAddressDescriptor> getExternals();
}
