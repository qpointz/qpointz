package io.qpointz.mill.ai.nlsql.processors;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.proto.Field;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.*;

@AllArgsConstructor
public class ExecuteQueryProcessor implements ChatCallPostProcessor {

    @Getter
    private final DataOperationDispatcher dispatcher;

    public record ExecutionResult(List<List<Object>> data, List<String> fields) implements QueryResult.DataContainer {}

    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        if (! result.containsKey("sql")) {
            return result;
        }

        val sql = result.get("sql").toString();
        val execution = execute(sql);
        val updated = new HashMap<>(result);
        updated.put("data", execution);
        return updated;
    }


    public QueryResult execute(String sql) {
        val request = QueryRequest.newBuilder()
                .setConfig(QueryExecutionConfig.newBuilder()
                        .setFetchSize(1000)
                        .build())
                .setStatement(SQLStatement.newBuilder()
                        .setSql(sql)
                        .build())
                .build();

        val result = this.dispatcher.execute(request);

        val fields = result.schema()
                .getFieldsList().stream()
                .sorted(Comparator.comparingInt(Field::getFieldIdx))
                .map(Field::getName)
                .toList();

        val sz = fields.size();

        List<List<Object>> records = new ArrayList<>();

        while (result.hasNext()) {
            val iter = new VectorBlockRecordIterator(List.of(result.next()).iterator()) {
                @Override
                public void close() {
                }
            };

            while (iter.next()) {
                val rl = new ArrayList<>(sz);
                for (int idx=0;idx<sz;idx++) {
                    rl.add(iter.getObject(idx));
                }
                records.add(rl);
            }
        }

        return new QueryResult(new ExecutionResult(records, fields));
    }
}
