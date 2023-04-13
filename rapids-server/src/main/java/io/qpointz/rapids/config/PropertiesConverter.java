package io.qpointz.rapids.config;

import lombok.AllArgsConstructor;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.Map;
import java.util.Properties;

public class PropertiesConverter implements Converter<PropertiesEntry> {

    @Override
    public PropertiesEntry convert(String value) throws IllegalArgumentException, NullPointerException {
        return PropertiesEntry.fromString(value);
    }
}
