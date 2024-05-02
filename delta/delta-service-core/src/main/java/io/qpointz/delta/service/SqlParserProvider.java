package io.qpointz.delta.service;

import io.substrait.proto.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public interface SqlParserProvider {

    @AllArgsConstructor
    @Builder
    class ParseResult {

        @Getter
        public String originalSql;

        @Getter
        boolean success;

        @Getter
        Throwable exception;

        @Getter
        String message;

        @Getter
        Plan plan;


    }
    boolean getAcceptsSql();

    ParseResult parse(String sql);

}
