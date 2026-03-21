package io.qpointz.mill.security.authorization.policy.expression;

import lombok.*;

/**
 * Holds a raw SQL expression string that is parsed lazily at evaluation time.
 * Supports backward compatibility with string-literal expressions in policy YAML.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class RawExpressionNode implements ExpressionNode {

    private String expression;

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visitRaw(this);
    }
}
