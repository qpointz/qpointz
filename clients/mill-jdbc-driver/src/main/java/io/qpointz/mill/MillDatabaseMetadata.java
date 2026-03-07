package io.qpointz.mill;

import io.qpointz.mill.metadata.database.*;
import io.qpointz.mill.proto.DialectDescriptor;
import io.qpointz.mill.proto.GetDialectRequest;
import io.qpointz.mill.proto.GetDialectResponse;
import io.qpointz.mill.proto.HandshakeRequest;
import io.qpointz.mill.proto.HandshakeResponse;
import io.qpointz.mill.proto.MetaInfoKey;
import io.qpointz.mill.proto.MetaInfoValue;
import lombok.Getter;
import lombok.val;

import java.sql.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * MillDatabaseMetadata is an implementation of the {@link DatabaseMetaData} interface.
 * This class provides metadata about the database in context, including capabilities,
 * limits, and other properties of the database.
 *
 * <ul>
 * <li>Most methods return default values such as {@code false}, {@code 0}, or empty strings, 
 * indicating that the features are not supported or information is not available.</li>
 * 
 * <li>This class can be used as a stub or placeholder implementation when an actual 
 * database connection is not available or when only basic metadata needs to be specified.</li>
 * </ul>
 *
 * Methods Overview:
 * <ul>
 * <li>{@link #allProceduresAreCallable()} and {@link #allTablesAreSelectable()} 
 * return whether all procedures are callable and all tables are selectable, respectively.</li>
 * 
 * <li>{@link #getURL()} and {@link #getUserName()} return empty strings, representing the 
 * connection URL and username.</li>
 *
 * <li>{@link #isReadOnly()} indicates whether the database is read-only.</li>
 *
 * <li>Various methods like {@link #supportsAlterTableWithAddColumn()} and {@link #supportsBatchUpdates()} 
 * return {@code false}, indicating that these features are not supported.</li>
 *
 * <li>Methods like {@link #getMaxBinaryLiteralLength()} and {@link #getMaxCharLiteralLength()} 
 * return {@code 0}, representing various constraints and limits in the database such as the 
 * maximum length of binary and character literals.</li>
 *
 * <li>{@link #getConnection()} returns {@code null}, indicating no active connection object is provided.</li>
 *
 * <li>Additional methods provide default or null responses for other metadata queries.</li>
 * </ul>
 *
 * Note: All methods that throw {@link SQLException} simply throw the exception when called, 
 * as this class is a stub implementation.
 */
public class MillDatabaseMetadata implements DatabaseMetaData {
    private final MillConnection connection;

    public MillDatabaseMetadata(MillConnection millConnection) {
        this.connection = millConnection;
    }

    @Getter(lazy = true)
    private final HandshakeResponse handshake = callHandshake();

    @Getter(lazy = true)
    private final Optional<GetDialectResponse> dialectResponse = callGetDialect();

    protected Map<Integer, MetaInfoValue> getMeta() {
        val hs = getHandshake();
        return hs.getMetasMap();
    }

    protected <T> T getMetaOr(MetaInfoKey key, T or, Predicate<MetaInfoValue> typeCheck, Function<MetaInfoValue, T> getValue) {
        val value = getMeta().getOrDefault(key.getNumber(), null);
        if (value == null) {
            return or;
        }
        if (!typeCheck.test(value)) {
            throw new ClassCastException(String.format("'%s' cast failed", key));
        }
        return getValue.apply(value);
    }

    private HandshakeResponse callHandshake()  {
        try {
            return connection.getClient()
                    .handshake(HandshakeRequest.getDefaultInstance());
        } catch (MillCodeException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<GetDialectResponse> callGetDialect() {
        try {
            if (!getHandshake().hasCapabilities() || !getHandshake().getCapabilities().getSupportDialect()) {
                return Optional.empty();
            }
            val response = connection.getClient().getDialect(GetDialectRequest.getDefaultInstance());
            if (response == null || !response.hasDialect()) {
                return Optional.empty();
            }
            return Optional.of(response);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Optional<DialectDescriptor> dialect() {
        return getDialectResponse().map(GetDialectResponse::getDialect);
    }

    private boolean hasDialect() {
        return dialect().isPresent();
    }

    private String dialectIdOrDefault() {
        return dialect().map(DialectDescriptor::getId).filter(k -> !k.isBlank()).orElse("CALCITE");
    }

    private String dialectNameOrDefault() {
        return dialect().map(DialectDescriptor::getName).filter(k -> !k.isBlank()).orElse("Apache Calcite");
    }

    private boolean featureFlag(String key, boolean defaultValue) {
        val d = dialect().orElse(null);
        if (d == null) {
            return defaultValue;
        }
        val flags = d.getFeatureFlagsMap();
        if (!flags.containsKey(key)) {
            return defaultValue;
        }
        return flags.get(key);
    }

    private int isolationLevelFromDialect() {
        val value = dialect()
                .map(DialectDescriptor::getTransactions)
                .map(DialectDescriptor.Transactions::getDefaultIsolation)
                .orElse("NONE");
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "READ_UNCOMMITTED" -> Connection.TRANSACTION_READ_UNCOMMITTED;
            case "READ_COMMITTED" -> Connection.TRANSACTION_READ_COMMITTED;
            case "REPEATABLE_READ" -> Connection.TRANSACTION_REPEATABLE_READ;
            case "SERIALIZABLE" -> Connection.TRANSACTION_SERIALIZABLE;
            case "NONE" -> Connection.TRANSACTION_NONE;
            default -> Connection.TRANSACTION_NONE;
        };
    }

    private String csvFunctionNames(String category) {
        val d = dialect().orElse(null);
        if (d == null) {
            return "";
        }
        val entries = d.getFunctionsMap()
                .getOrDefault(category, DialectDescriptor.FunctionCategory.getDefaultInstance())
                .getEntriesList();
        return entries.stream()
                .map(DialectDescriptor.FunctionEntry::getName)
                .filter(k -> k != null && !k.isBlank())
                .distinct()
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    private int parseVersionPart(int idx) {
        final String source;
        try {
            source = getDatabaseProductVersion();
        } catch (SQLException e) {
            return 0;
        }
        val matcher = Pattern.compile("(\\d+)").matcher(source);
        int current = 0;
        while (matcher.find()) {
            if (current == idx) {
                return Integer.parseInt(matcher.group(1));
            }
            current++;
        }
        return 0;
    }

    /**
     * Indicates whether all procedures can be called by the current user.
     * 
     * <p>
     * This implementation always returns {@code false}, indicating that not 
     * all procedures are callable.
     * 
     * @return {@code false}, indicating not all procedures are callable.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    /**
     * Indicates whether all tables are selectable by the current user.
     * 
     * <p>
     * This implementation always returns {@code true}, implying that the current user 
     * can select from all tables. This stub implementation does not actually verify 
     * any permissions or database properties.
     * 
     * @return {@code true}, indicating all tables are selectable.
     * @throws SQLException if a database access error occurs.
     */
    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return true;
    }

    @Override
    public String getURL() throws SQLException {
        return this.connection.getUrl();
    }

    @Override
    public String getUserName() throws SQLException {
        val handshakeUser =  getHandshake()
                .getAuthentication()
                .getName();
        return  handshakeUser.isBlank() || handshakeUser.isEmpty() || handshakeUser.equals("ANONYMOUS")
                ? ""
                : handshakeUser;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return dialect().map(DialectDescriptor::getReadOnly).orElse(true);
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return dialect().map(DialectDescriptor::getNullSorting).map(DialectDescriptor.NullSorting::getNullsSortedHigh).orElse(false);
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return dialect().map(DialectDescriptor::getNullSorting).map(DialectDescriptor.NullSorting::getNullsSortedLow).orElse(true);
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return dialect().map(DialectDescriptor::getNullSorting).map(DialectDescriptor.NullSorting::getNullsSortedAtStart).orElse(false);
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return dialect().map(DialectDescriptor::getNullSorting).map(DialectDescriptor.NullSorting::getNullsSortedAtEnd).orElse(false);
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return dialectNameOrDefault();
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return getDialectResponse()
                .map(GetDialectResponse::getSchemaVersion)
                .filter(k -> !k.isBlank())
                .orElse(getHandshake().getVersion().name());
    }


    private static final String DRIVER_NAME = "Mill JDBC driver";

    @Override
    public String getDriverName() throws SQLException {
        return DRIVER_NAME;
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return String.format("%s.%s.%s", Driver.majorVersion, Driver.minorVersion, Driver.buildVersion);
    }

    @Override
    public int getDriverMajorVersion() {
        return Driver.majorVersion;
    }

    @Override
    public int getDriverMinorVersion() {
        return Driver.minorVersion;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getSupportsMixedCase).orElse(false);
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getUnquotedStorage)
                .map(k -> "UPPER".equalsIgnoreCase(k)).orElse(true);
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getUnquotedStorage)
                .map(k -> "LOWER".equalsIgnoreCase(k)).orElse(false);
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getUnquotedStorage)
                .map(k -> "AS_IS".equalsIgnoreCase(k)).orElse(false);
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getSupportsMixedCaseQuoted).orElse(true);
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getQuotedStorage)
                .map(k -> "UPPER".equalsIgnoreCase(k)).orElse(false);
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getQuotedStorage)
                .map(k -> "LOWER".equalsIgnoreCase(k)).orElse(false);
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getQuotedStorage)
                .map(k -> "AS_IS".equalsIgnoreCase(k)).orElse(true);
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers)
                .map(DialectDescriptor.Identifiers::getQuote)
                .map(DialectDescriptor.QuotePair::getStart)
                .filter(k -> !k.isBlank())
                .orElse(" ");
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return dialect().map(DialectDescriptor::getStringProperties).map(DialectDescriptor.StringProperties::getSqlKeywords).orElse("");
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return csvFunctionNames("numerics");
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return csvFunctionNames("strings");
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return dialect().map(DialectDescriptor::getStringProperties).map(DialectDescriptor.StringProperties::getSystemFunctions).orElse("");
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return csvFunctionNames("dates-times");
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return dialect().map(DialectDescriptor::getStringProperties).map(DialectDescriptor.StringProperties::getSearchStringEscape).orElse("");
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return dialect().map(DialectDescriptor::getIdentifiers).map(DialectDescriptor.Identifiers::getExtraNameCharacters).orElse("");
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return featureFlag("supports-alter-table-add-column", false);
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return featureFlag("supports-alter-table-drop-column", false);
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return featureFlag("supports-column-aliasing", true);
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return featureFlag("null-plus-non-null-is-null", true);
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return featureFlag("supports-convert", true);
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return this.supportsConvert();
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return featureFlag("supports-table-correlation-names", true);
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return featureFlag("supports-different-table-correlation-names", false);
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return featureFlag("supports-expressions-in-order-by", true);
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return featureFlag("supports-order-by-unrelated", false);
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return featureFlag("supports-group-by", true);
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return featureFlag("supports-group-by-unrelated", false);
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return featureFlag("supports-group-by-beyond-select", false);
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return featureFlag("supports-like-escape-clause", true);
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return featureFlag("supports-multiple-result-sets", false);
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return dialect().map(DialectDescriptor::getTransactions).map(DialectDescriptor.Transactions::getSupportsMultiple).orElse(false);
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return featureFlag("supports-non-nullable-columns", false);
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return featureFlag("supports-minimum-sql-grammar", true);
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return featureFlag("supports-core-sql-grammar", true);
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return featureFlag("supports-extended-sql-grammar", false);
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return featureFlag("supports-ansi92-entry-level", true);
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return featureFlag("supports-ansi92-intermediate", false);
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return featureFlag("supports-ansi92-full", false);
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return featureFlag("supports-integrity-enhancement", false);
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return featureFlag("supports-outer-joins", true);
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return featureFlag("supports-full-outer-joins", true);
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return featureFlag("supports-limited-outer-joins", true);
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getSchemaTerm).orElse("schema");
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getProcedureTerm).orElse("procedure");
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogTerm).orElse("catalog");
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogAtStart).orElse(true);
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogSeparator).orElse(".");
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getSchemasInDml).orElse(true);
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getSchemasInProcedureCalls).orElse(false);
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getSchemasInTableDefinitions).orElse(false);
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getSchemasInIndexDefinitions).orElse(false);
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getSchemasInPrivilegeDefinitions).orElse(false);
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogsInDml).orElse(false);
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogsInProcedureCalls).orElse(false);
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogsInTableDefinitions).orElse(false);
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogsInIndexDefinitions).orElse(false);
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return dialect().map(DialectDescriptor::getCatalogSchema).map(DialectDescriptor.CatalogSchema::getCatalogsInPrivilegeDefinitions).orElse(false);
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return featureFlag("supports-positioned-delete", false);
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return featureFlag("supports-positioned-update", false);
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return featureFlag("supports-select-for-update", false);
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return featureFlag("supports-stored-procedures", false);
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return featureFlag("supports-subqueries-in-comparisons", true);
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return featureFlag("supports-subqueries-in-exists", true);
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return featureFlag("supports-subqueries-in-ins", true);
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return featureFlag("supports-subqueries-in-quantifieds", true);
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return featureFlag("supports-correlated-subqueries", true);
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return featureFlag("supports-union", true);
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return featureFlag("supports-union-all", true);
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxBinaryLiteralLength).orElse(0);
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxCharLiteralLength).orElse(0);
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxColumnNameLength).orElse(0);
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxColumnsInGroupBy).orElse(0);
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxColumnsInIndex).orElse(0);
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxColumnsInOrderBy).orElse(0);
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxColumnsInSelect).orElse(0);
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxColumnsInTable).orElse(0);
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxConnections).orElse(0);
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxIndexLength).orElse(0);
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxSchemaNameLength).orElse(0);
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxCatalogNameLength).orElse(0);
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxRowSize).orElse(0);
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxRowSizeIncludesBlobs).orElse(false);
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxStatementLength).orElse(0);
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxStatements).orElse(0);
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxTableNameLength).orElse(0);
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return dialect().map(DialectDescriptor::getLimits).map(DialectDescriptor.Limits::getMaxTablesInSelect).orElse(0);
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return isolationLevelFromDialect();
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return dialect().map(DialectDescriptor::getTransactions).map(DialectDescriptor.Transactions::getSupported).orElse(false);
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return supportsTransactions() && level == getDefaultTransactionIsolation();
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return dialect().map(DialectDescriptor::getTransactions).map(DialectDescriptor.Transactions::getSupportsDdlAndDml).orElse(false);
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return dialect().map(DialectDescriptor::getTransactions).map(DialectDescriptor.Transactions::getSupportsDmlOnly).orElse(false);
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return dialect().map(DialectDescriptor::getTransactions).map(DialectDescriptor.Transactions::getDdlCausesCommit).orElse(false);
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return dialect().map(DialectDescriptor::getTransactions).map(DialectDescriptor.Transactions::getDdlIgnoredInTransactions).orElse(false);
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        return new TablesMetadata(this.connection, catalog, schemaPattern, tableNamePattern, types).asResultSet();
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return new SchemasMetadata(this.connection)
                .asResultSet();
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        return CatalogsMetadata.getInstance()
                .asResultSet();
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        return new TableTypesMetadata()
                .asResultSet();
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return new ColumnsMetadata(this.connection, catalog, schemaPattern, tableNamePattern, columnNamePattern).asResultSet();
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return new TypeInfoMetadata(dialect().map(DialectDescriptor::getTypeInfoList).orElse(List.of())).asResultSet();
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        return null;
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        val resultSet = dialect().map(DialectDescriptor::getResultSet).orElse(null);
        if (resultSet == null) {
            return type == ResultSet.TYPE_FORWARD_ONLY;
        }
        return switch (type) {
            case ResultSet.TYPE_FORWARD_ONLY -> resultSet.getForwardOnly();
            case ResultSet.TYPE_SCROLL_INSENSITIVE -> resultSet.getScrollInsensitive();
            case ResultSet.TYPE_SCROLL_SENSITIVE -> resultSet.getScrollSensitive();
            default -> false;
        };
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        val resultSet = dialect().map(DialectDescriptor::getResultSet).orElse(null);
        if (resultSet == null) {
            return type == ResultSet.TYPE_FORWARD_ONLY && concurrency == ResultSet.CONCUR_READ_ONLY;
        }
        if (concurrency == ResultSet.CONCUR_READ_ONLY) {
            return resultSet.getConcurrencyReadOnly() && supportsResultSetType(type);
        }
        if (concurrency == ResultSet.CONCUR_UPDATABLE) {
            return resultSet.getConcurrencyUpdatable() && supportsResultSetType(type);
        }
        return false;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return featureFlag("supports-batch-updates", false);
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return featureFlag("supports-savepoints", false);
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return featureFlag("supports-named-parameters", false);
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return featureFlag("supports-multiple-open-results", false);
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return featureFlag("supports-get-generated-keys", false);
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        return null;
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return parseVersionPart(0);
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return parseVersionPart(1);
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 2;
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return DatabaseMetaData.sqlStateSQL;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return featureFlag("supports-statement-pooling", false);
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return new SchemasMetadata(this.connection)
                .asResultSet();
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return featureFlag("supports-stored-functions-using-call-syntax", false);
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return featureFlag("auto-commit-failure-closes-all-result-sets", false);
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return featureFlag("generated-key-always-returned", false);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return iface.cast(this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(MillDatabaseMetadata.class);
    }
}
