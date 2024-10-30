package io.qpointz.mill.services.configuration;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import io.qpointz.mill.services.meta.ServiceDescriptor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnService("grpc")
public class GrpcServiceDescriptor implements ServiceDescriptor {

    @Override
    public String getStereotype() {
        return "grpc";
    }

}
