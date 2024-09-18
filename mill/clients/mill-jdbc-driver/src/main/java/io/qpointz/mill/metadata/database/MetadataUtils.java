package io.qpointz.mill.metadata.database;

import java.util.Optional;

public class MetadataUtils {

    public static Optional<String> stringOf(String value) {
        return value == null || value.isEmpty() || value.isBlank()
                ? Optional.empty()
                : Optional.of(value);
    }

    public static Optional<Integer> integerOf(Integer value) {
        return value == null
                ? Optional.empty()
                : Optional.of(value);
    }

    public static Optional<Integer> integerOf(Integer value, Integer nullValue) {
        return value == null || value.equals(nullValue)
                ? Optional.empty()
                : Optional.of(value);
    }

    public static <T> Optional<T> dbnull() {
        return Optional.empty();
    }

}
