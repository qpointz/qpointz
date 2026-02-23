package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.service.annotations.ConditionalOnService;
import io.qpointz.mill.service.descriptors.ServiceDescriptor;
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
