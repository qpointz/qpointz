package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.data.backend.annotations.ConditionalOnService;
import io.qpointz.mill.data.backend.descriptors.ServiceDescriptor;
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
