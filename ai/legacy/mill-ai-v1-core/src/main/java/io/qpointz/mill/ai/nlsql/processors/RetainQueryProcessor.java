package io.qpointz.mill.ai.nlsql.processors;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public final class RetainQueryProcessor implements ChatCallPostProcessor {

    private static final String QUERY_KEY = "query";

    @Getter
    private final String query;

    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        if (result.containsKey(QUERY_KEY)) {
            log.warn("{} key already contains in result. Skipping postprocessing.", QUERY_KEY);
            return result;
        }
        val m = new HashMap<>(result);
        m.put(QUERY_KEY, query);
        return m;
    }
}
