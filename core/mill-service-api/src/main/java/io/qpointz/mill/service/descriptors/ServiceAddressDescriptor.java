package io.qpointz.mill.service.descriptors;

import lombok.val;

import java.net.URL;

public record ServiceAddressDescriptor(
        ServiceAddressScheme scheme,
        String host,
        Integer port
) {
    public String asUrl() {
        val builder = new StringBuilder();
        builder.append(scheme.toString().toLowerCase())
                .append("://")
                .append(host)
                .append(portOf(scheme, port));
        return builder.toString();
    }

    private String portOf(ServiceAddressScheme scheme, Integer port) {
        if ((scheme == ServiceAddressScheme.HTTP && port == 80) ||
            (scheme == ServiceAddressScheme.HTTPS && port == 443)) {
            return "";
        } else {
            return ":" + port.toString();
        }
    }
}
