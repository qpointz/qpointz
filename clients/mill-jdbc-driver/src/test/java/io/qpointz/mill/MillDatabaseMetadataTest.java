package io.qpointz.mill;

import io.qpointz.mill.client.MillClient;
import io.qpointz.mill.proto.DialectDescriptor;
import io.qpointz.mill.proto.GetDialectResponse;
import io.qpointz.mill.proto.HandshakeResponse;
import io.qpointz.mill.proto.ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MillDatabaseMetadataTest {

    @Test
    void shouldMapDialectIntoDatabaseMetadata() throws Exception {
        MillConnection connection = mock(MillConnection.class);
        MillClient client = mock(MillClient.class);
        when(connection.getClient()).thenReturn(client);
        when(connection.getUrl()).thenReturn("jdbc:mill://localhost:9090");

        HandshakeResponse handshake = HandshakeResponse.newBuilder()
                .setVersion(ProtocolVersion.V1_0)
                .setCapabilities(HandshakeResponse.Capabilities.newBuilder()
                        .setSupportSql(true)
                        .setSupportResultPaging(true)
                        .setSupportDialect(true)
                        .build())
                .setAuthentication(HandshakeResponse.AuthenticationContext.newBuilder().setName("ANONYMOUS").build())
                .build();
        when(client.handshake(org.mockito.ArgumentMatchers.any())).thenReturn(handshake);

        DialectDescriptor dialect = DialectDescriptor.newBuilder()
                .setId("H2")
                .setName("H2 Database")
                .setReadOnly(false)
                .setParamstyle("qmark")
                .setIdentifiers(DialectDescriptor.Identifiers.newBuilder()
                        .setQuote(DialectDescriptor.QuotePair.newBuilder().setStart("\"").setEnd("\"").build())
                        .setAliasQuote(DialectDescriptor.QuotePair.newBuilder().setStart("\"").setEnd("\"").build())
                        .setEscapeQuote("\"")
                        .setUnquotedStorage("AS_IS")
                        .setQuotedStorage("AS_IS")
                        .setSupportsMixedCase(true)
                        .setSupportsMixedCaseQuoted(true)
                        .setMaxLength(256)
                        .setExtraNameCharacters("")
                        .setUseFullyQualifiedNames(true)
                        .build())
                .setCatalogSchema(DialectDescriptor.CatalogSchema.newBuilder()
                        .setSupportsSchemas(true)
                        .setSupportsCatalogs(false)
                        .setCatalogSeparator(".")
                        .setCatalogAtStart(true)
                        .setSchemaTerm("schema")
                        .setCatalogTerm("catalog")
                        .setProcedureTerm("procedure")
                        .setSchemasInDml(true)
                        .build())
                .setTransactions(DialectDescriptor.Transactions.newBuilder()
                        .setSupported(true)
                        .setDefaultIsolation("READ_COMMITTED")
                        .setSupportsMultiple(true)
                        .setSupportsDdlAndDml(true)
                        .setSupportsDmlOnly(false)
                        .setDdlCausesCommit(false)
                        .setDdlIgnoredInTransactions(false)
                        .build())
                .setLimits(DialectDescriptor.Limits.newBuilder()
                        .setMaxColumnNameLength(256)
                        .setMaxTableNameLength(256)
                        .setMaxBinaryLiteralLength(0)
                        .setMaxCharLiteralLength(0)
                        .build())
                .setNullSorting(DialectDescriptor.NullSorting.newBuilder()
                        .setNullsSortedLow(true)
                        .setSupportsNullsFirst(true)
                        .setSupportsNullsLast(true)
                        .build())
                .setResultSet(DialectDescriptor.ResultSetCaps.newBuilder()
                        .setForwardOnly(true)
                        .setScrollInsensitive(false)
                        .setScrollSensitive(false)
                        .setConcurrencyReadOnly(true)
                        .setConcurrencyUpdatable(false)
                        .build())
                .setStringProperties(DialectDescriptor.StringProperties.newBuilder()
                        .setSearchStringEscape("\\")
                        .setSqlKeywords("LIMIT")
                        .setSystemFunctions("DATABASE,USER")
                        .build())
                .putFeatureFlags("supports-group-by", true)
                .putFeatureFlags("supports-column-aliasing", true)
                .putFeatureFlags("supports-batch-updates", false)
                .putFeatureFlags("supports-get-generated-keys", true)
                .putFeatureFlags("supports-statement-pooling", false)
                .putFunctions("strings", DialectDescriptor.FunctionCategory.newBuilder()
                        .addEntries(DialectDescriptor.FunctionEntry.newBuilder()
                                .setName("UPPER")
                                .setReturnType(DialectDescriptor.ReturnType.newBuilder().setType("STRING").setNullable(false).build())
                                .setSyntax("UPPER({expr})")
                                .build())
                        .build())
                .putFunctions("numerics", DialectDescriptor.FunctionCategory.newBuilder()
                        .addEntries(DialectDescriptor.FunctionEntry.newBuilder()
                                .setName("ABS")
                                .setReturnType(DialectDescriptor.ReturnType.newBuilder().setType("DOUBLE").setNullable(false).build())
                                .setSyntax("ABS({expr})")
                                .build())
                        .build())
                .putFunctions("dates-times", DialectDescriptor.FunctionCategory.newBuilder()
                        .addEntries(DialectDescriptor.FunctionEntry.newBuilder()
                                .setName("CURRENT_DATE")
                                .setReturnType(DialectDescriptor.ReturnType.newBuilder().setType("DATE").setNullable(false).build())
                                .setSyntax("CURRENT_DATE")
                                .build())
                        .build())
                .addTypeInfo(DialectDescriptor.TypeInfo.newBuilder()
                        .setSqlName("INTEGER")
                        .setJdbcTypeCode(Types.INTEGER)
                        .setPrecision(10)
                        .setSearchable(DatabaseMetaData.typeSearchable)
                        .setUnsigned(false)
                        .setNumPrecRadix(10)
                        .build())
                .build();

        GetDialectResponse getDialectResponse = GetDialectResponse.newBuilder()
                .setDialect(dialect)
                .setSchemaVersion("v2")
                .setContentHash("hash")
                .build();
        when(client.getDialect(org.mockito.ArgumentMatchers.any())).thenReturn(getDialectResponse);

        MillDatabaseMetadata metadata = new MillDatabaseMetadata(connection);

        assertEquals("jdbc:mill://localhost:9090", metadata.getURL());
        assertEquals("H2 Database", metadata.getDatabaseProductName());
        assertEquals("v2", metadata.getDatabaseProductVersion());
        assertFalse(metadata.isReadOnly());
        assertEquals("\"", metadata.getIdentifierQuoteString());
        assertTrue(metadata.supportsGroupBy());
        assertTrue(metadata.supportsColumnAliasing());
        assertTrue(metadata.supportsGetGeneratedKeys());
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, metadata.getDefaultTransactionIsolation());
        assertTrue(metadata.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
        assertTrue(metadata.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
        assertFalse(metadata.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
        assertEquals(metadata.getConnection(), connection);

        try (ResultSet typeInfo = metadata.getTypeInfo()) {
            assertTrue(typeInfo.next());
            assertEquals("INTEGER", typeInfo.getString("TYPE_NAME"));
            assertEquals(Types.INTEGER, typeInfo.getInt("DATA_TYPE"));
            assertEquals(10, typeInfo.getInt("PRECISION"));
        }
    }
}
