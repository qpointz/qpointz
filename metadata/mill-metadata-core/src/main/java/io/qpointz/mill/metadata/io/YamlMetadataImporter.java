package io.qpointz.mill.metadata.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qpointz.mill.metadata.domain.MetadataEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class YamlMetadataImporter implements MetadataImporter {

    private final ObjectMapper yamlMapper;

    public YamlMetadataImporter() {
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Collection<MetadataEntity> importFrom(InputStream source) throws IOException {
        Map<String, List<MetadataEntity>> wrapper = yamlMapper.readValue(
                source, new TypeReference<>() {});
        return wrapper.getOrDefault("entities", List.of());
    }
}
