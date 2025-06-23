package io.qpointz.mill.services.configuration;

import io.substrait.extension.SimpleExtension;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix="mill.backend")
@Configuration
public class BackendConfiguration {

    @Getter
    @Setter
    private String  provider;

    @Getter
    @Setter
    private Map<String, String> connection;

    @Bean
    public SimpleExtension.ExtensionCollection substraitExtensionCollection() throws IOException {
        return SimpleExtension.loadDefaults();
    }

}
