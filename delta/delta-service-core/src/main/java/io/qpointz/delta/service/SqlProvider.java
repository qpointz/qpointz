package io.qpointz.delta.service;

import io.substrait.proto.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public interface SqlProvider {

    @AllArgsConstructor
    @Builder
    class ParseResult {

        @Getter
        private String originalSql;

        @Getter
        private boolean success;

        @Getter
        private Throwable exception;

        @Getter
        private String message;

        @Getter
        private Plan plan;

    }

    ParseResult parseSql(String sql);

}
