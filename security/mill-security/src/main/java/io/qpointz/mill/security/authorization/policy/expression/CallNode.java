package io.qpointz.mill.security.authorization.policy.expression;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CallNode implements ExpressionNode {

    private String operator;

    @Builder.Default
    private List<ExpressionNode> operands = List.of();

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor) {
        return visitor.visitCall(this);
    }

    public static CallNode of(String operator, ExpressionNode... operands) {
        return new CallNode(operator, List.of(operands));
    }

    public static CallNode eq(ExpressionNode left, ExpressionNode right) {
        return of("eq", left, right);
    }

    public static CallNode ne(ExpressionNode left, ExpressionNode right) {
        return of("ne", left, right);
    }

    public static CallNode and(ExpressionNode... operands) {
        return of("and", operands);
    }

    public static CallNode or(ExpressionNode... operands) {
        return of("or", operands);
    }

    public static CallNode not(ExpressionNode operand) {
        return of("not", operand);
    }

    public static CallNode gt(ExpressionNode left, ExpressionNode right) {
        return of("gt", left, right);
    }

    public static CallNode ge(ExpressionNode left, ExpressionNode right) {
        return of("ge", left, right);
    }

    public static CallNode lt(ExpressionNode left, ExpressionNode right) {
        return of("lt", left, right);
    }

    public static CallNode le(ExpressionNode left, ExpressionNode right) {
        return of("le", left, right);
    }

    public static CallNode like(ExpressionNode left, ExpressionNode right) {
        return of("like", left, right);
    }

    public static CallNode in(ExpressionNode value, ExpressionNode... elements) {
        var ops = new java.util.ArrayList<ExpressionNode>();
        ops.add(value);
        ops.addAll(List.of(elements));
        return new CallNode("in", ops);
    }

    public static CallNode between(ExpressionNode value, ExpressionNode low, ExpressionNode high) {
        return of("between", value, low, high);
    }
}
