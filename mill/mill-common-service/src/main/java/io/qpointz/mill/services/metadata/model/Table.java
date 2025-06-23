package io.qpointz.mill.services.metadata.model;

import java.util.Collection;
import java.util.Optional;

public record Table(String schema, String name, Collection<Attribute> attributes, Optional<String> description) {
}
