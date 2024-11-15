package io.qpointz.mill.plan;

import io.substrait.dsl.SubstraitBuilder;
import io.substrait.expression.Expression;
import io.substrait.extension.DefaultExtensionCatalog;
import io.substrait.type.TypeCreator;

public class LogicalFunctionHelper {

    private final SubstraitBuilder builder;

    static final TypeCreator R = TypeCreator.of(false);
    static final TypeCreator N = TypeCreator.of(true);

    public LogicalFunctionHelper(SubstraitBuilder builder) {
        this.builder = builder;
    }

    public Expression or(Expression... args) {
        return this.builder.or(args);
    }

    public Expression and(Expression... args) {
        return this.builder.scalarFn(DefaultExtensionCatalog.FUNCTIONS_BOOLEAN, "and:bool", N.BOOLEAN, args);
    }

    public Expression xor(Expression arg1, Expression arg2) {
        return this.builder.scalarFn(DefaultExtensionCatalog.FUNCTIONS_BOOLEAN, "xor:bool", N.BOOLEAN, arg1, arg2);
    }

    public Expression not(Expression arg) {
        return this.builder.scalarFn(DefaultExtensionCatalog.FUNCTIONS_BOOLEAN, "not:bool", N.BOOLEAN, arg);
    }

}
