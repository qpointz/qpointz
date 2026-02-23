package io.qpointz.mill.metadata.domain;

import java.util.Optional;

public interface FacetClassResolver {
    Optional<Class<? extends MetadataFacet>> resolve(String typeKey);
}
