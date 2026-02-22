package io.qpointz.mill.data.backend.metadata.model;


import java.util.Optional;

public record Relation(TableRef source, TableRef target, AttributeRelation attributeRelation, Cardinality cardinality, Optional<String> description) {

    public record TableRef(String schema, String name) {}

    public record AttributeRef(String name) {}

    public record AttributeRelation(AttributeRef source, AttributeRef target) {}

    public enum Cardinality {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY,
        UNSPECIFIED
    }


}
