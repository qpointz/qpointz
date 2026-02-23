package io.qpointz.mill.metadata.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qpointz.mill.metadata.domain.MetadataEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class YamlMetadataExporter implements MetadataExporter {

    private final ObjectMapper yamlMapper;

    public YamlMetadataExporter() {
        YAMLFactory factory = new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        this.yamlMapper = new ObjectMapper(factory);
        this.yamlMapper.registerModule(new JavaTimeModule());
        this.yamlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void export(Collection<MetadataEntity> entities, OutputStream target) throws IOException {
        yamlMapper.writeValue(target, Map.of("entities", entities));
    }
}
