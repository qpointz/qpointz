package io.qpointz.mill.client;

import io.qpointz.mill.MillCodeException;
import io.qpointz.mill.MillConnection;
import io.qpointz.mill.proto.*;
import io.qpointz.mill.sql.VectorBlockRecordIterator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Builder
@AllArgsConstructor
public class MillSqlQuery {

    @Getter
    private final MillConnection connection;

    @Getter
    private final String sql;

    @Getter
    @Builder.Default
    private final int fetchSize = MillClientConfiguration.DEFAULT_FETCH_SIZE ;

    @Getter
    @Builder.Default
    private  final Set<Integer> selectedIndexes = new HashSet<>();

    @Getter
    @Builder.Default
    private  final Set<String> selectedNames = new HashSet<>();

    public static class MillSqlQueryBuilder {

        public MillSqlQueryBuilder selectedIndexed(int[] indexes) {
            if (indexes != null && indexes.length > 0) {
                val set = IntStream.of(indexes).boxed().collect(Collectors.toSet());
                this.selectedIndexes(set);
            }
            return this;
        }

        public MillSqlQueryBuilder selectedNamesArray(String[] columnNames) {
            if (columnNames != null && columnNames.length > 0) {
                Set<String> set = Arrays.stream(columnNames).collect(Collectors.toSet());
                this.selectedNames(set);
            }
            return this;
        }

    }

    private QueryExecutionConfig.Builder queryConfig() {
        val queryConfig = QueryExecutionConfig.newBuilder()
                .setFetchSize(this.getFetchSize());

        val attributes = QueryExecutionConfig.Attributes.newBuilder();
        if (this.getSelectedIndexes()!=null && !this.getSelectedIndexes().isEmpty()) {
            attributes.addAllIndexes(this.getSelectedIndexes());
        }

        if (this.getSelectedNames()!=null && !this.getSelectedNames().isEmpty()) {
            attributes.addAllNames(this.getSelectedNames());
        }

        queryConfig.setAttributes(attributes);

        return queryConfig;
    }

    private SQLStatement.Builder statement() {
        val statement = SQLStatement.newBuilder();
        statement.setSql(this.getSql());
        return statement;
    }

    private QueryRequest.Builder request() {
        return QueryRequest.newBuilder()
              .setConfig(this.queryConfig())
              .setStatement(this.statement());
    }

    public MillQueryResult executeResult() throws MillCodeException {
        val client = this.connection.getClient();
        val request = this.request()
                .build();
        return client.execQuery(request);
    }

    public Iterator<VectorBlock> executeVectorBlocks() throws MillCodeException {
        return this.executeResult().getVectorBlocks();
    }

    private class RecordIterator extends VectorBlockRecordIterator {
        protected RecordIterator(Iterator<VectorBlock> vectorBlocks) {
            super(vectorBlocks);
        }

        @Override
        public void close() {
            //no closable associated resources
        }
    }

    public VectorBlockRecordIterator executeRecordIterator() throws MillCodeException {
        return new RecordIterator(this.executeVectorBlocks());
    }

}
