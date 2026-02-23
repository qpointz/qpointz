package io.qpointz.mill.metadata.domain;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFacetClassResolver implements FacetClassResolver {

    private final Map<String, Class<? extends MetadataFacet>> mappings = new ConcurrentHashMap<>();

    public DefaultFacetClassResolver() {}

    public DefaultFacetClassResolver(Map<String, Class<? extends MetadataFacet>> initial) {
        if (initial != null) {
            mappings.putAll(initial);
        }
    }

    public void register(String typeKey, Class<? extends MetadataFacet> facetClass) {
        mappings.put(typeKey, facetClass);
    }

    @Override
    public Optional<Class<? extends MetadataFacet>> resolve(String typeKey) {
        return Optional.ofNullable(mappings.get(typeKey));
    }
}
