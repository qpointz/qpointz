package io.qpointz.mill.security.authorization.policy.expression;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class LiteralNode implements ExpressionNode {

    private Object value;

    private String dataType;

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visitLiteral(this);
    }
}
