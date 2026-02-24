package io.qpointz.mill.service.descriptors;

public record ServiceAddressDescriptor(
        ServiceAddressScheme scheme,
        String host,
        Integer port
) {
}
