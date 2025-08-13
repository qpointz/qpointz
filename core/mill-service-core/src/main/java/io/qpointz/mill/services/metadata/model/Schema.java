package io.qpointz.mill.services.metadata.model;

import java.util.Optional;

public record Schema(String name, Optional<String> description) {
}
