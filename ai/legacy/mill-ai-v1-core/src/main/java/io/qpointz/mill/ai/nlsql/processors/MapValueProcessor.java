package io.qpointz.mill.ai.nlsql.processors;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.ai.nlsql.ChatEventProducer;
import io.qpointz.mill.ai.nlsql.ValueMapper;
import io.qpointz.mill.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

/**
 * Post processor that replaces placeholder tokens in generated SQL with values resolved by a
 * {@link ValueMapper}. The placeholders originate from the NL2SQL model and take the form
 * {@code @{target:placeholder}} within the SQL text.
 */
@AllArgsConstructor
@Slf4j
public class MapValueProcessor implements ChatCallPostProcessor {

    @Getter
    private final ValueMapper valueMapper;

    @Getter
    private final ChatEventProducer eventProducer;

    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        eventProducer.beginProgressEvent("Prepare query...");
        val proc =  new HashMap<String, Object>();
        proc.putAll(result);

        val mayBeSql = proc.getOrDefault("sql", null);
        if (mayBeSql == null) {
            return proc;
        }

        var sql = mayBeSql.toString();
        if (sql.isBlank()) {
            return proc;
        }

        @SuppressWarnings("unchecked")
        val mappings = (List<Object>)proc.getOrDefault("value-mapping", List.of());
        if (mappings.isEmpty()) {
            return proc;
        }

        val placeholders = mappings.stream()
                .map(f-> JsonUtils.defaultJsonMapper().convertValue(f, ValueMapper.PlaceholderMapping.class))
                .toList();

        for (val p : placeholders) {
            val repl = String.format("@{%s:%s}", p.target(), p.placeholder());
            log.info("Sql value postprocessing: {}", p.target());
            val mappedValue =this.valueMapper.mapValue(p);
            sql = sql.replace(repl, mappedValue.mappedValue());
        }

        proc.put("sql", sql);

        eventProducer.endProgressEvent();
        return proc;
    }

}
