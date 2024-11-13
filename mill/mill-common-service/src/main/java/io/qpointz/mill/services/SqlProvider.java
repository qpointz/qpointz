package io.qpointz.mill.services;

import io.substrait.expression.Expression;
import io.substrait.plan.Plan;
import lombok.*;

import java.util.List;

public interface SqlProvider {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class PlanParseResult {

        private PlanParseResult() {}

        @Getter
        private Plan plan;

        @Getter
        private RuntimeException exception;

        public boolean isSuccess() {
            return exception == null;
        }

        public String getMessage() {
            return this.exception == null
                    ? null
                    : this.exception.getMessage();
        }

        public static PlanParseResult success(Plan plan) {
            return new PlanParseResult( plan, null);
        }

        public static PlanParseResult fail(Exception exception) {
            return PlanParseResult.fail(new RuntimeException(exception));
        }

        public static PlanParseResult fail(RuntimeException th) {
            return new PlanParseResult( null, th);
        }

        public static PlanParseResult fail(String message) {
            return new PlanParseResult( null, new IllegalArgumentException(message));
        }

    }

    record ExpressionParseResult (boolean isSuccess, Expression expression, Exception exception) {

        public static ExpressionParseResult fail(Exception exception) {
            return new ExpressionParseResult(false, null, exception);
        }

    }

    default boolean supportsSql() {
        return true;
    }

    PlanParseResult parseSql(String sql);

    ExpressionParseResult parseSqlExpression(List<String> tableName, String expression);

}
