package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.service.descriptors.ServiceDescriptor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnService(value = "grpc", group = "data")
public class GrpcServiceDescriptor implements ServiceDescriptor {

    public record HostDescriptor(String external) {}

    private final HostDescriptor host;

    public GrpcServiceDescriptor(
            @Value("${mill.data.services.grpc.host.external:}") String externalHostReference
    ) {
        this.host = externalHostReference == null || externalHostReference.isBlank()
                ? null
                : new HostDescriptor(externalHostReference);
    }

    @Override
    public String getStereotype() {
        return "grpc";
    }

}
