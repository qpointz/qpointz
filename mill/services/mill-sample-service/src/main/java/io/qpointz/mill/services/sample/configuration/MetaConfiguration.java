package io.qpointz.mill.services.sample.configuration;


import io.qpointz.mill.services.descriptors.ServiceDescriptor;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetaConfiguration {

    @Bean
    ServiceDescriptor serviceDescriptor() {
        return new SimpleServiceDescriptor("simple", "hallo");
    }

    public record SimpleServiceDescriptor(@Getter String stereotype,
                                          @Getter String port) implements ServiceDescriptor {
    }


}
