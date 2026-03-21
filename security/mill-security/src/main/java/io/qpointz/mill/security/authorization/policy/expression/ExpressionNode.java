package io.qpointz.mill.security.authorization.policy.expression;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "nodeType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LiteralNode.class, name = "literal"),
        @JsonSubTypes.Type(value = FieldRefNode.class, name = "fieldRef"),
        @JsonSubTypes.Type(value = CallNode.class, name = "call"),
        @JsonSubTypes.Type(value = CastNode.class, name = "cast"),
        @JsonSubTypes.Type(value = NullCheckNode.class, name = "nullCheck"),
        @JsonSubTypes.Type(value = RawExpressionNode.class, name = "raw")
})
public sealed interface ExpressionNode
        permits LiteralNode, FieldRefNode, CallNode, CastNode, NullCheckNode, RawExpressionNode {

    <T> T accept(ExpressionNodeVisitor<T> visitor);
}
