package io.qpointz.mill.ai.nlsql.models;

import io.qpointz.mill.ai.chat.messages.MessageSpec;

import java.util.Set;

public interface SqlDialect {

    String getId();

    record SqlFeatures(
            boolean identifiers,
            boolean literals,
            boolean joins,
            boolean ordering,
            boolean grouping,
            boolean paging,
            Set<String> operators,
            Set<String> functions
    ) {
        public static final SqlFeatures DEFAULT =  new SqlFeatures(true, true, true, true, true, true,
                Set.of("equality", "comparison", "like", "between", "regex"),
                Set.of("strings", "regex", "numerics", "aggregates", "dates_times", "conditionals"));
    }

    MessageSpec getConventionsSpec(SqlFeatures features);
}
