package io.qpointz.mill.security.authorization.policy.model;

import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.ExpressionNode;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyActionEntry implements Serializable {

    private ActionVerb verb;

    private String type;

    @Builder.Default
    private List<String> table = List.of();

    private ExpressionNode expression;

    private String rawExpression;

    @Builder.Default
    private Boolean exclusive = false;

    private List<String> columns;

    private ColumnsMode columnsMode;

    public boolean isExclusive() {
        return Boolean.TRUE.equals(exclusive);
    }

    public boolean hasExpression() {
        return expression != null || (rawExpression != null && !rawExpression.isBlank());
    }

    public boolean hasColumns() {
        return columns != null && !columns.isEmpty();
    }
}
