package io.qpointz.mill.metadata;

import java.util.Optional;

public class MetadataUtils {

    public static Optional<String> stringOf(String value) {
        return value == null || value.isEmpty() || value.isBlank()
                ? Optional.empty()
                : Optional.of(value);
    }

    public static <T> Optional<T> dbnull() {
        return Optional.<T>empty();
    }

}
