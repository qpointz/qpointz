package io.qpointz.mill.data.backend.metadata.model;

import java.util.Optional;

public record Schema(String name, Optional<String> description) {
}
