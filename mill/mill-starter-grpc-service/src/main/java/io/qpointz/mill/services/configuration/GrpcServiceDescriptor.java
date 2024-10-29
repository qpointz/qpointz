package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.meta.ServiceDescriptor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class GrpcServiceDescriptor implements ServiceDescriptor {

    @Override
    public String getStereotype() {
        return "grpc";
    }

}
