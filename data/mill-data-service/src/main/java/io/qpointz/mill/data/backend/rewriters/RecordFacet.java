package io.qpointz.mill.data.backend.rewriters;

import io.substrait.expression.Expression;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class RecordFacet {

    @Getter
    private final Expression expression;

}
