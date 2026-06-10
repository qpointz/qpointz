package io.qpointz.mill.ai.data.valuemap

import io.qpointz.mill.ai.valuemap.ColumnDistinctValueLoader
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher
import io.qpointz.mill.proto.QueryExecutionConfig
import io.qpointz.mill.proto.QueryRequest
import io.qpointz.mill.proto.SQLStatement
import io.qpointz.mill.sql.RecordReaders
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec

/**
 * DISTINCT loader backed by [DataOperationDispatcher.execute] (same pattern as Skymill vector IT).
 */
class DataOperationColumnDistinctValueLoader(
    private val dispatcher: DataOperationDispatcher,
    private val sqlSpec: SqlDialectSpec
) : ColumnDistinctValueLoader {

    override fun loadDistinctQuoted(schema: String, table: String, column: String, includeNull: Boolean): List<String?> {
        val sql =
            "SELECT DISTINCT ${quote(column)} FROM ${quote(schema)}.${quote(table)}"
        val request = QueryRequest.newBuilder()
            .setStatement(SQLStatement.newBuilder().setSql(sql).build())
            .setConfig(QueryExecutionConfig.newBuilder().setFetchSize(5000).build())
            .build()
        val reader = RecordReaders.recordReader(dispatcher.execute(request))
        val out = mutableListOf<String?>()
        while (reader.next()) {
            if (reader.isNull(0)) {
                if (includeNull) {
                    out.add(null)
                }
            } else {
                out.add(reader.getString(0))
            }
        }
        reader.close()
        return out
    }

    private fun quote(ident: String): String {
        val quote = sqlSpec.identifiers.quote
        return "${quote.start}$ident${quote.end}"
    }
}
