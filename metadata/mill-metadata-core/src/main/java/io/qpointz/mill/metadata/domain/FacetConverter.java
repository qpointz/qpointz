package io.qpointz.mill.metadata.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Optional;

public class FacetConverter {

    private static final FacetConverter DEFAULT = new FacetConverter(createDefaultMapper());

    private final ObjectMapper objectMapper;

    public FacetConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static FacetConverter defaultConverter() {
        return DEFAULT;
    }

    public <T> Optional<T> convert(Object raw, Class<T> facetClass) {
        if (raw == null) {
            return Optional.empty();
        }
        if (facetClass.isInstance(raw)) {
            return Optional.of(facetClass.cast(raw));
        }
        try {
            T deserialized = objectMapper.convertValue(raw, facetClass);
            return Optional.of(deserialized);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static ObjectMapper createDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        return mapper;
    }
}
