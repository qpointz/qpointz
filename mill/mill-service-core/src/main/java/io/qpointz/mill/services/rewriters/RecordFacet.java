package io.qpointz.mill.services.rewriters;

import io.substrait.expression.Expression;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class RecordFacet {

    @Getter
    private final Expression expression;

}
