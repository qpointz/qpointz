package io.qpointz.mill.services.configuration;

import io.qpointz.mill.MillRuntimeDataException;
import io.substrait.extension.SimpleExtension;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
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
        val defaultCollection =  SimpleExtension.loadDefaults();
        //val overrides = load( "/extensions/functions.yml");
        //val full = overrides.merge(defaultCollection);
        return defaultCollection;
    }

    private SimpleExtension.ExtensionCollection load(String location) {
        try(val in = BackendConfiguration.class.getResourceAsStream(location)) {
            return SimpleExtension.load(location, in);
        } catch (IOException io) {
            throw new MillRuntimeDataException(io);
        }

    }

}
