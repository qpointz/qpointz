package io.qpointz.mill.metadata.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qpointz.mill.metadata.domain.MetadataEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonMetadataImporter implements MetadataImporter {

    private final ObjectMapper objectMapper;

    public JsonMetadataImporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Collection<MetadataEntity> importFrom(InputStream source) throws IOException {
        Map<String, List<MetadataEntity>> wrapper = objectMapper.readValue(
                source, new TypeReference<>() {});
        return wrapper.getOrDefault("entities", List.of());
    }
}
