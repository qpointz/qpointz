package io.qpointz.mill.security.authorization.policy.expression;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class NullCheckNode implements ExpressionNode {

    private ExpressionNode operand;

    @Builder.Default
    private boolean negated = false;

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visitNullCheck(this);
    }

    public static NullCheckNode isNull(ExpressionNode operand) {
        return new NullCheckNode(operand, false);
    }

    public static NullCheckNode isNotNull(ExpressionNode operand) {
        return new NullCheckNode(operand, true);
    }
}
