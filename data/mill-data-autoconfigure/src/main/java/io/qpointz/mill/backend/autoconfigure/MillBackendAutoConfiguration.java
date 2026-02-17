package io.qpointz.mill.backend.autoconfigure;

import io.substrait.extension.SimpleExtension;
import lombok.val;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

public class MillBackendAutoConfiguration {

    @Bean
    public SimpleExtension.ExtensionCollection substraitExtensionCollection() throws IOException {
        val defaultCollection =  SimpleExtension.loadDefaults();
        return defaultCollection;
    }

}
