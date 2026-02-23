package io.qpointz.mill.security.authorization.policy.expression;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CastNode implements ExpressionNode {

    private ExpressionNode operand;

    private String targetType;

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visitCast(this);
    }
}
