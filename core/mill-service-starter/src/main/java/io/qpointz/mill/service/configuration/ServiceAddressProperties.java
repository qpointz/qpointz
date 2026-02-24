package io.qpointz.mill.service.configuration;

import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "mill.application.hosts")
public class ServiceAddressProperties {

    private Map<String, ServiceAddressDescriptor> externals = new HashMap<>();

    public Map<String, ServiceAddressDescriptor> getExternals() {
        return externals;
    }

    public void setExternals(Map<String, ServiceAddressDescriptor> externals) {
        this.externals = externals == null ? new HashMap<>() : externals;
    }
}
