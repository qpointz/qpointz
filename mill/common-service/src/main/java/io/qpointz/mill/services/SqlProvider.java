package io.qpointz.mill.services;

import io.substrait.plan.Plan;
import lombok.*;

public interface SqlProvider {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class ParseResult {

        private ParseResult() {}

        @Getter
        private Plan plan;

        @Getter
        private Throwable exception;

        public boolean isSuccess() {
            return exception == null;
        }

        public String getMessage() {
            return this.exception == null
                    ? null
                    : this.exception.getMessage();
        }

        public static ParseResult success(Plan plan) {
            return new ParseResult( plan, null);
        }

        public static ParseResult fail(Throwable th) {
            return new ParseResult( null, th);
        }

        public static ParseResult fail(String message) {
            return new ParseResult( null, new IllegalArgumentException(message));
        }

    }

    ParseResult parseSql(String sql);

}
