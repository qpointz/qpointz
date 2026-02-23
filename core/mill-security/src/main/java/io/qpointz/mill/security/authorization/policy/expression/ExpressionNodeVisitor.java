package io.qpointz.mill.security.authorization.policy.expression;

public interface ExpressionNodeVisitor<T> {
    T visitLiteral(LiteralNode node);
    T visitFieldRef(FieldRefNode node);
    T visitCall(CallNode node);
    T visitCast(CastNode node);
    T visitNullCheck(NullCheckNode node);
    T visitRaw(RawExpressionNode node);
}
