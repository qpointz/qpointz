package io.qpointz.mill.data.backend.access.http.configuration;

import io.qpointz.mill.service.annotations.ConditionalOnService;
import io.qpointz.mill.service.descriptors.ServiceDescriptor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnService(value = "http", group = "data")
public class HttpServiceDescriptor implements ServiceDescriptor {

    public record HostDescriptor(String external) {}

    private final HostDescriptor host;

    public HttpServiceDescriptor(
            @Value("${mill.services.jet-http.host.external:}") String externalHostReference
    ) {
        this.host = externalHostReference == null || externalHostReference.isBlank()
                ? null
                : new HostDescriptor(externalHostReference);
    }

    @Override
    public String getStereotype() {
        return "jet-http";
    }
}
