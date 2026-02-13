package io.qpointz.mill.ai.nlsql.processors;

import io.qpointz.mill.ai.chat.ChatCallPostProcessor;
import io.qpointz.mill.ai.nlsql.ChatEventProducer;
import io.qpointz.mill.proto.Field;
import io.qpointz.mill.proto.QueryExecutionConfig;
import io.qpointz.mill.proto.QueryRequest;
import io.qpointz.mill.proto.SQLStatement;
import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;

@Slf4j
@AllArgsConstructor
public class SubmitQueryProcessor implements ChatCallPostProcessor {

    @Getter
    private final DataOperationDispatcher dispatcher;

    @Getter
    private final int resultPageSize;

    @Getter
    private final ChatEventProducer eventProducer;

    public record PagingResult(String continueLink, List<List<Object>> data, List<String> fields) implements QueryResult.DataContainer {}

    @Override
    public Map<String, Object> process(Map<String, Object> result) {
        if (! result.containsKey("sql")) {
            return result;
        }

        val sql = result.get("sql").toString();
        val updated = new HashMap<>(result);
        try {
            eventProducer.beginProgressEvent("Execute query");
            val execution = submit(sql);
            updated.put("data", execution);
            eventProducer.endProgressEvent();
        } catch (Exception ex) {
            eventProducer.chatErrorEvent(ex.getMessage());
            val error = ex.getMessage();
            log.error("Error submitting SQL: {}", sql, ex);
            updated.put("error", error);
        }

        return updated;
    }

    public QueryResult submit(String sql) {
        val request = QueryRequest.newBuilder()
                .setConfig(QueryExecutionConfig.newBuilder()
                        .setFetchSize(20)
                        .build())
                .setStatement(SQLStatement.newBuilder()
                        .setSql(sql)
                        .build())
                .build();
        val result = this.dispatcher.submitQuery(request);

        val iter = new VectorBlockRecordIterator(List.of(result.getVector()).iterator()) {
            @Override
            public void close() {
            }
        };

        val schema = result.getVector().getSchema().getFieldsList();
        val fields = schema.stream()
                .sorted(Comparator.comparingInt(Field::getFieldIdx))
                .map(Field::getName)
                .toList();
        val sz = fields.size();

        List<List<Object>> records = new ArrayList(result.getVector().getVectorSize());
        while (iter.next()) {
            val rl = new ArrayList<>(sz);
            for (int idx=0;idx<sz;idx++) {
                rl.add(iter.getObject(idx));
            }
            records.add(rl);
        }

        return new QueryResult(new PagingResult(result.getPagingId(), records, fields));
    }
}
