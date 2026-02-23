package io.qpointz.mill.metadata.domain;

import java.util.List;

public record ValidationResult(boolean valid, List<String> errors) {

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(String error) {
        return new ValidationResult(false, List.of(error));
    }

    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, errors);
    }

    public static ValidationResult merge(List<ValidationResult> results) {
        var errors = results.stream()
                .filter(r -> !r.valid())
                .flatMap(r -> r.errors().stream())
                .toList();
        return errors.isEmpty() ? ok() : fail(errors);
    }
}
