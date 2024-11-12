package io.qpointz.mill.services.dispatchers;

import io.qpointz.mill.proto.*;
import io.qpointz.mill.vectors.VectorBlockIterator;

public interface DataOperationDispatcher {
    HandshakeResponse handshake(HandshakeRequest request);

    ListSchemasResponse listSchemas(ListSchemasRequest listSchemasRequest);

    GetSchemaResponse getSchema(GetSchemaRequest getSchemaRequest);

    ParseSqlResponse parseSql(ParseSqlRequest parseSqlRequest);

    QueryResultResponse submitQuery(QueryRequest queryRequest);

    QueryResultResponse fetchResult(QueryResultRequest queryResultRequest);

    VectorBlockIterator execute(QueryRequest request);

}
