package io.qpointz.mill.security.authorization.policy.actions;

import io.qpointz.mill.security.authorization.policy.Action;
import io.qpointz.mill.security.authorization.policy.PolicyActionDescriptor;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public final class ExpressionFilterAction implements Action {

    @Getter
    private final List<String> tableName;

    @Getter
    private final String expression;

    @Getter
    @Accessors(fluent = true)
    @Builder.Default
    private final Boolean isNegate = false;

    public static Action fromDescriptor(PolicyActionDescriptor k) {
        val params = k.getParams();
        val name = params.getOrDefault("name", "").toString();
        if (name ==null || name.isEmpty() || name.isBlank()) {
            throw new IllegalArgumentException("Expression action object 'name' is null or empty:"+k);
        }
        val qName = List.of(name.split("\\."));

        val expression = params.getOrDefault("expression", "").toString();
        if (expression == null || expression.isEmpty() || expression.isBlank()) {
            throw new IllegalArgumentException("Expression action object 'expression' is null or empty:"+k);
        }

        return new ExpressionFilterAction(qName, expression, false);

    }

    @Override
    public List<String> subject() {
        return this.tableName;
    }

    @Override
    public String actionName() {
        return "rel-filter";
    }

    public boolean isEmpty() {
        return this.getExpression() == null || this.getExpression().isEmpty();
    }
}
