package io.qpointz.mill.services.metadata;

import lombok.Getter;

public record ColumnAnnotations(String name, String description) implements Annotations {
}
