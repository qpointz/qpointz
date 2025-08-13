//package io.qpointz.mill.ai.nlsql.components;
//
//import io.qpointz.mill.proto.Field;
//import io.qpointz.mill.proto.QueryExecutionConfig;
//import io.qpointz.mill.proto.QueryRequest;
//import io.qpointz.mill.proto.SQLStatement;
//import io.qpointz.mill.services.dispatchers.DataOperationDispatcher;
//import io.qpointz.mill.sql.VectorBlockRecordIterator;
//import lombok.val;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
//public class QueryExecutor {
//
//    private final DataOperationDispatcher dataDispatcher;
//
//    public record ExecutionResult(String continueLink, List<List<Object>> data, List<String> fields) {}
//
//    public QueryExecutor(DataOperationDispatcher dataDispatcher) {
//        this.dataDispatcher = dataDispatcher;
//    }
//
//    public ExecutionResult submit(String sql) {
//        val request = QueryRequest.newBuilder()
//                .setConfig(QueryExecutionConfig.newBuilder()
//                        .setFetchSize(20)
//                        .build())
//                .setStatement(SQLStatement.newBuilder()
//                        .setSql(sql)
//                        .build())
//                .build();
//        val result = this.dataDispatcher.submitQuery(request);
//        val iter = new VectorBlockRecordIterator(List.of(result.getVector()).iterator()) {
//            @Override
//            public void close() {
//            }
//        };
//
//        val schema = result.getVector().getSchema().getFieldsList();
//        val fields = schema.stream()
//                .sorted(Comparator.comparingInt(Field::getFieldIdx))
//                .map(Field::getName)
//                .toList();
//        val sz = fields.size();
//
//        List<List<Object>> records = new ArrayList(result.getVector().getVectorSize());
//        while (iter.next()) {
//            val rl = new ArrayList<>(sz);
//            for (int idx=0;idx<sz;idx++) {
//                rl.add(iter.getObject(idx));
//            }
//            records.add(rl);
//        }
//
//        return new ExecutionResult(result.getPagingId(), records, fields);
//    }
//
//}
