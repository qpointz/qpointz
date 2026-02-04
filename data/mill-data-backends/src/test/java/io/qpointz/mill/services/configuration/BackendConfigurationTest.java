package io.qpointz.mill.services.configuration;

import io.substrait.extension.SimpleExtension;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BackendConfigurationTest {

    @Test
    void doesntFails() throws IOException {
        val bc = new BackendConfiguration();
        assertDoesNotThrow(bc::substraitExtensionCollection);
        bc.substraitExtensionCollection()
                .scalarFunctions()
                .stream()
                .sorted(Comparator.comparing(SimpleExtension.Function::name))
                .forEach(k-> log.info(k.name()) );

    }


}