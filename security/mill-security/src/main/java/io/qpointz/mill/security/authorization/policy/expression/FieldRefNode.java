package io.qpointz.mill.security.authorization.policy.expression;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class FieldRefNode implements ExpressionNode {

    private String fieldName;

    private Integer fieldIndex;

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visitFieldRef(this);
    }
}
