package io.qpointz.mill.security.authorization.policy.expression;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionNodeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void literalNode_stringValue() {
        var node = LiteralNode.builder().value("hello").build();
        assertEquals("hello", node.getValue());
    }

    @Test
    void literalNode_numericValue() {
        var node = LiteralNode.builder().value(42).build();
        assertEquals(42, node.getValue());
    }

    @Test
    void literalNode_booleanValue() {
        var node = LiteralNode.builder().value(true).build();
        assertEquals(true, node.getValue());
    }

    @Test
    void literalNode_nullValue() {
        var node = LiteralNode.builder().value(null).build();
        assertNull(node.getValue());
    }

    @Test
    void fieldRefNode_byName() {
        var node = FieldRefNode.builder().fieldName("department").build();
        assertEquals("department", node.getFieldName());
        assertNull(node.getFieldIndex());
    }

    @Test
    void fieldRefNode_byIndex() {
        var node = FieldRefNode.builder().fieldIndex(3).build();
        assertEquals(3, node.getFieldIndex());
    }

    @Test
    void callNode_eq() {
        var node = CallNode.eq(
                FieldRefNode.builder().fieldName("department").build(),
                LiteralNode.builder().value("analytics").build()
        );
        assertEquals("eq", node.getOperator());
        assertEquals(2, node.getOperands().size());
    }

    @Test
    void callNode_and() {
        var node = CallNode.and(
                CallNode.eq(
                        FieldRefNode.builder().fieldName("status").build(),
                        LiteralNode.builder().value("ACTIVE").build()
                ),
                CallNode.eq(
                        FieldRefNode.builder().fieldName("region").build(),
                        LiteralNode.builder().value("US").build()
                )
        );
        assertEquals("and", node.getOperator());
        assertEquals(2, node.getOperands().size());
    }

    @Test
    void callNode_not() {
        var inner = CallNode.eq(
                FieldRefNode.builder().fieldName("status").build(),
                LiteralNode.builder().value("archived").build()
        );
        var node = CallNode.not(inner);
        assertEquals("not", node.getOperator());
        assertEquals(1, node.getOperands().size());
    }

    @Test
    void callNode_between() {
        var node = CallNode.between(
                FieldRefNode.builder().fieldName("age").build(),
                LiteralNode.builder().value(18).build(),
                LiteralNode.builder().value(65).build()
        );
        assertEquals("between", node.getOperator());
        assertEquals(3, node.getOperands().size());
    }

    @Test
    void callNode_in() {
        var node = CallNode.in(
                FieldRefNode.builder().fieldName("region").build(),
                LiteralNode.builder().value("US").build(),
                LiteralNode.builder().value("EU").build()
        );
        assertEquals("in", node.getOperator());
        assertEquals(3, node.getOperands().size());
    }

    @Test
    void castNode() {
        var node = CastNode.builder()
                .operand(FieldRefNode.builder().fieldName("amount").build())
                .targetType("DECIMAL")
                .build();
        assertEquals("DECIMAL", node.getTargetType());
    }

    @Test
    void nullCheckNode_isNull() {
        var node = NullCheckNode.isNull(FieldRefNode.builder().fieldName("email").build());
        assertFalse(node.isNegated());
    }

    @Test
    void nullCheckNode_isNotNull() {
        var node = NullCheckNode.isNotNull(FieldRefNode.builder().fieldName("email").build());
        assertTrue(node.isNegated());
    }

    @Test
    void rawExpressionNode() {
        var node = RawExpressionNode.builder().expression("department = 'analytics'").build();
        assertEquals("department = 'analytics'", node.getExpression());
    }

    @Test
    void jsonRoundtrip_callNode() throws Exception {
        var original = CallNode.and(
                CallNode.eq(
                        FieldRefNode.builder().fieldName("status").build(),
                        LiteralNode.builder().value("ACTIVE").build()
                ),
                CallNode.not(
                        CallNode.eq(
                                FieldRefNode.builder().fieldName("archived").build(),
                                LiteralNode.builder().value(true).build()
                        )
                )
        );

        var json = mapper.writeValueAsString(original);
        var restored = mapper.readValue(json, ExpressionNode.class);
        assertEquals(original, restored);
    }

    @Test
    void jsonRoundtrip_literalNode() throws Exception {
        var original = LiteralNode.builder().value("test").dataType("VARCHAR").build();
        var json = mapper.writeValueAsString(original);
        var restored = mapper.readValue(json, ExpressionNode.class);
        assertEquals(original, restored);
    }

    @Test
    void jsonRoundtrip_rawExpressionNode() throws Exception {
        var original = RawExpressionNode.builder().expression("x > 10").build();
        var json = mapper.writeValueAsString(original);
        var restored = mapper.readValue(json, ExpressionNode.class);
        assertEquals(original, restored);
    }

    @Test
    void jsonRoundtrip_deepNesting() throws Exception {
        var original = CallNode.and(
                CallNode.or(
                        CallNode.eq(
                                FieldRefNode.builder().fieldName("a").build(),
                                LiteralNode.builder().value(1).build()
                        ),
                        CallNode.gt(
                                FieldRefNode.builder().fieldName("b").build(),
                                LiteralNode.builder().value(100).build()
                        )
                ),
                NullCheckNode.isNotNull(FieldRefNode.builder().fieldName("c").build()),
                CastNode.builder()
                        .operand(FieldRefNode.builder().fieldName("d").build())
                        .targetType("INTEGER")
                        .build()
        );

        var json = mapper.writeValueAsString(original);
        var restored = mapper.readValue(json, ExpressionNode.class);
        assertEquals(original, restored);
    }

    @Test
    void visitor_traversal() {
        var node = CallNode.eq(
                FieldRefNode.builder().fieldName("x").build(),
                LiteralNode.builder().value(42).build()
        );

        var result = node.accept(new ExpressionNodeVisitor<String>() {
            @Override
            public String visitLiteral(LiteralNode n) { return "literal:" + n.getValue(); }
            @Override
            public String visitFieldRef(FieldRefNode n) { return "field:" + n.getFieldName(); }
            @Override
            public String visitCall(CallNode n) { return "call:" + n.getOperator(); }
            @Override
            public String visitCast(CastNode n) { return "cast"; }
            @Override
            public String visitNullCheck(NullCheckNode n) { return "null"; }
            @Override
            public String visitRaw(RawExpressionNode n) { return "raw"; }
        });

        assertEquals("call:eq", result);
    }
}
