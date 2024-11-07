package io.qpointz.mill.services.configuration;

import io.substrait.extension.SimpleExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class BackendConfiguration {

    @Bean
    public static SimpleExtension.ExtensionCollection substraitExtensionCollection() throws IOException {
        return SimpleExtension.loadDefaults();
    }

}
