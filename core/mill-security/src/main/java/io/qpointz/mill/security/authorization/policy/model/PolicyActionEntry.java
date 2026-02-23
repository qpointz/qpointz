package io.qpointz.mill.security.authorization.policy.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import io.qpointz.mill.security.authorization.policy.ActionVerb;
import io.qpointz.mill.security.authorization.policy.expression.ExpressionNode;
import io.qpointz.mill.security.authorization.policy.expression.ExpressionNodeParser;
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

    /**
     * Supports dual-mode policy expression import:
     * - textual value -> rawExpression
     * - structured node -> expression AST
     */
    @JsonSetter("expression")
    public void setExpressionFromJson(JsonNode node) {
        if (node == null || node.isNull()) {
            this.expression = null;
            this.rawExpression = null;
            return;
        }
        if (node.isTextual()) {
            this.rawExpression = node.asText();
            this.expression = null;
            return;
        }
        this.expression = ExpressionNodeParser.parse(node);
        this.rawExpression = null;
    }
}
