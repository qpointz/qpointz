package io.qpointz.mill.metadata.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qpointz.mill.metadata.domain.MetadataEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class JsonMetadataExporter implements MetadataExporter {

    private final ObjectMapper objectMapper;

    public JsonMetadataExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void export(Collection<MetadataEntity> entities, OutputStream target) throws IOException {
        objectMapper.writeValue(target, Map.of("entities", entities));
    }
}
