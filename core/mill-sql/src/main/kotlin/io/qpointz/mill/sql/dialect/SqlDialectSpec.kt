package io.qpointz.mill.sql.v2.dialect

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = false)
data class SqlDialectSpec(
    val id: String,
    val name: String,
    @JsonProperty("read-only") val readOnly: Boolean,
    val paramstyle: String,
    val notes: List<String> = emptyList(),
    val identifiers: Identifiers,
    @JsonProperty("catalog-schema") val catalogSchema: CatalogSchema,
    val transactions: Transactions,
    val limits: Limits,
    @JsonProperty("null-sorting") val nullSorting: NullSorting,
    @JsonProperty("result-set") val resultSet: ResultSetCaps,
    @JsonProperty("feature-flags") val featureFlags: Map<String, Boolean?> = emptyMap(),
    @JsonProperty("string-properties") val stringProperties: StringProperties,
    val literals: Literals,
    val joins: Joins,
    val paging: Paging,
    val operators: Map<String, List<OperatorEntry>> = emptyMap(),
    val functions: Map<String, List<FunctionEntry>> = emptyMap(),
    @JsonProperty("type-info") val typeInfo: List<TypeInfo> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class QuotePair(
    val start: String,
    val end: String
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class Identifiers(
    val quote: QuotePair,
    @JsonProperty("alias-quote") val aliasQuote: QuotePair,
    @JsonProperty("escape-quote") val escapeQuote: String,
    @JsonProperty("unquoted-storage") val unquotedStorage: String,
    @JsonProperty("quoted-storage") val quotedStorage: String,
    @JsonProperty("supports-mixed-case") val supportsMixedCase: Boolean,
    @JsonProperty("supports-mixed-case-quoted") val supportsMixedCaseQuoted: Boolean,
    @JsonProperty("max-length") val maxLength: Int,
    @JsonProperty("extra-name-characters") val extraNameCharacters: String,
    @JsonProperty("use-fully-qualified-names") val useFullyQualifiedNames: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class CatalogSchema(
    @JsonProperty("supports-schemas") val supportsSchemas: Boolean,
    @JsonProperty("supports-catalogs") val supportsCatalogs: Boolean,
    @JsonProperty("catalog-separator") val catalogSeparator: String,
    @JsonProperty("catalog-at-start") val catalogAtStart: Boolean,
    @JsonProperty("schema-term") val schemaTerm: String,
    @JsonProperty("catalog-term") val catalogTerm: String,
    @JsonProperty("procedure-term") val procedureTerm: String,
    @JsonProperty("schemas-in-dml") val schemasInDml: Boolean,
    @JsonProperty("schemas-in-procedure-calls") val schemasInProcedureCalls: Boolean,
    @JsonProperty("schemas-in-table-definitions") val schemasInTableDefinitions: Boolean,
    @JsonProperty("schemas-in-index-definitions") val schemasInIndexDefinitions: Boolean,
    @JsonProperty("schemas-in-privilege-definitions") val schemasInPrivilegeDefinitions: Boolean,
    @JsonProperty("catalogs-in-dml") val catalogsInDml: Boolean,
    @JsonProperty("catalogs-in-procedure-calls") val catalogsInProcedureCalls: Boolean,
    @JsonProperty("catalogs-in-table-definitions") val catalogsInTableDefinitions: Boolean,
    @JsonProperty("catalogs-in-index-definitions") val catalogsInIndexDefinitions: Boolean,
    @JsonProperty("catalogs-in-privilege-definitions") val catalogsInPrivilegeDefinitions: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class Transactions(
    val supported: Boolean,
    @JsonProperty("default-isolation") val defaultIsolation: String,
    @JsonProperty("supports-multiple") val supportsMultiple: Boolean,
    @JsonProperty("supports-ddl-and-dml") val supportsDdlAndDml: Boolean,
    @JsonProperty("supports-dml-only") val supportsDmlOnly: Boolean,
    @JsonProperty("ddl-causes-commit") val ddlCausesCommit: Boolean,
    @JsonProperty("ddl-ignored-in-transactions") val ddlIgnoredInTransactions: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class Limits(
    @JsonProperty("max-binary-literal-length") val maxBinaryLiteralLength: Int,
    @JsonProperty("max-char-literal-length") val maxCharLiteralLength: Int,
    @JsonProperty("max-column-name-length") val maxColumnNameLength: Int,
    @JsonProperty("max-columns-in-group-by") val maxColumnsInGroupBy: Int,
    @JsonProperty("max-columns-in-index") val maxColumnsInIndex: Int,
    @JsonProperty("max-columns-in-order-by") val maxColumnsInOrderBy: Int,
    @JsonProperty("max-columns-in-select") val maxColumnsInSelect: Int,
    @JsonProperty("max-columns-in-table") val maxColumnsInTable: Int,
    @JsonProperty("max-connections") val maxConnections: Int,
    @JsonProperty("max-index-length") val maxIndexLength: Int,
    @JsonProperty("max-schema-name-length") val maxSchemaNameLength: Int,
    @JsonProperty("max-catalog-name-length") val maxCatalogNameLength: Int,
    @JsonProperty("max-row-size") val maxRowSize: Int,
    @JsonProperty("max-row-size-includes-blobs") val maxRowSizeIncludesBlobs: Boolean,
    @JsonProperty("max-statement-length") val maxStatementLength: Int,
    @JsonProperty("max-statements") val maxStatements: Int,
    @JsonProperty("max-table-name-length") val maxTableNameLength: Int,
    @JsonProperty("max-tables-in-select") val maxTablesInSelect: Int
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class NullSorting(
    @JsonProperty("nulls-sorted-high") val nullsSortedHigh: Boolean,
    @JsonProperty("nulls-sorted-low") val nullsSortedLow: Boolean,
    @JsonProperty("nulls-sorted-at-start") val nullsSortedAtStart: Boolean,
    @JsonProperty("nulls-sorted-at-end") val nullsSortedAtEnd: Boolean,
    @JsonProperty("supports-nulls-first") val supportsNullsFirst: Boolean,
    @JsonProperty("supports-nulls-last") val supportsNullsLast: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class ResultSetCaps(
    @JsonProperty("forward-only") val forwardOnly: Boolean,
    @JsonProperty("scroll-insensitive") val scrollInsensitive: Boolean,
    @JsonProperty("scroll-sensitive") val scrollSensitive: Boolean,
    @JsonProperty("concurrency-read-only") val concurrencyReadOnly: Boolean,
    @JsonProperty("concurrency-updatable") val concurrencyUpdatable: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class StringProperties(
    @JsonProperty("search-string-escape") val searchStringEscape: String,
    @JsonProperty("sql-keywords") val sqlKeywords: String,
    @JsonProperty("system-functions") val systemFunctions: String
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class Literals(
    val strings: StringLiterals,
    val booleans: List<String>,
    @JsonProperty("null") val nullLiteral: String,
    @JsonProperty("dates-times") val datesTimes: DatesTimesLiterals
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class StringLiterals(
    val quote: String,
    val concat: String,
    val escape: String,
    val note: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class DateTimeLiteral(
    val syntax: String,
    val quote: String,
    val pattern: String,
    val notes: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class IntervalLiteral(
    val supported: Boolean,
    val style: String,
    val notes: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class DatesTimesLiterals(
    val date: DateTimeLiteral,
    val time: DateTimeLiteral,
    val timestamp: DateTimeLiteral,
    val interval: IntervalLiteral
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class Joins(
    val style: String,
    @JsonProperty("cross-join") val crossJoin: JoinType,
    @JsonProperty("inner-join") val innerJoin: JoinType,
    @JsonProperty("left-join") val leftJoin: JoinType,
    @JsonProperty("right-join") val rightJoin: JoinType,
    @JsonProperty("full-join") val fullJoin: JoinType,
    @JsonProperty("on-clause") val onClause: OnClause
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class JoinType(
    val enabled: Boolean? = null,
    val keyword: String? = null,
    @JsonProperty("require-on") val requireOn: Boolean? = null,
    @JsonProperty("null-safe") val nullSafe: Boolean? = null,
    val notes: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class OnClause(
    val keyword: String,
    @JsonProperty("require-condition") val requireCondition: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class Paging(
    val styles: List<PagingStyle>,
    val offset: String,
    @JsonProperty("no-limit-value") val noLimitValue: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class PagingStyle(
    val syntax: String,
    val type: String,
    val deprecated: Boolean? = null
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class OperatorEntry(
    val symbol: String,
    val syntax: String? = null,
    val description: String? = null,
    val supported: Boolean? = null,
    val deprecated: Boolean? = null
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class FunctionEntry(
    val name: String,
    val synonyms: List<String> = emptyList(),
    @JsonProperty("return") val returnType: ReturnType,
    val syntax: String,
    val args: List<FunctionArg> = emptyList(),
    val notes: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class ReturnType(
    val type: String,
    val nullable: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class FunctionArg(
    val name: String,
    val type: String,
    val required: Boolean,
    val variadic: Boolean? = null,
    val multi: Boolean? = null,
    val min: Int? = null,
    val max: Int? = null,
    @JsonProperty("enum") @JsonAlias("enu") val `enum`: List<String>? = null,
    @JsonProperty("default") val defaultValue: String? = null,
    val notes: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class TypeInfo(
    @JsonProperty("sql-name") val sqlName: String,
    @JsonProperty("jdbc-type-code") val jdbcTypeCode: Int,
    val precision: Int? = null,
    @JsonProperty("literal-prefix") val literalPrefix: String? = null,
    @JsonProperty("literal-suffix") val literalSuffix: String? = null,
    @JsonProperty("case-sensitive") val caseSensitive: Boolean? = null,
    val searchable: Int? = null,
    val unsigned: Boolean? = null,
    @JsonProperty("fixed-prec-scale") val fixedPrecScale: Boolean? = null,
    @JsonProperty("auto-increment") val autoIncrement: Boolean? = null,
    @JsonProperty("minimum-scale") val minimumScale: Int? = null,
    @JsonProperty("maximum-scale") val maximumScale: Int? = null,
    @JsonProperty("num-prec-radix") val numPrecRadix: Int? = null
)
