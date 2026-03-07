package io.qpointz.mill.sql.v2.dialect

import io.qpointz.mill.proto.DialectDescriptor
import io.qpointz.mill.proto.GetDialectResponse
import java.security.MessageDigest

object DialectProtoMapper {
    private const val DEFAULT_SCHEMA_VERSION = "v1"

    @JvmStatic
    fun toResponse(spec: SqlDialectSpec, schemaVersion: String = DEFAULT_SCHEMA_VERSION): GetDialectResponse {
        val descriptor = toDescriptor(spec)
        return GetDialectResponse.newBuilder()
            .setDialect(descriptor)
            .setSchemaVersion(schemaVersion)
            .setContentHash(contentHashHex(descriptor))
            .build()
    }

    @JvmStatic
    fun contentHashHex(descriptor: DialectDescriptor): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(descriptor.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    @JvmStatic
    fun toDescriptor(spec: SqlDialectSpec): DialectDescriptor =
        DialectDescriptor.newBuilder()
            .setId(spec.id)
            .setName(spec.name)
            .setReadOnly(spec.readOnly)
            .setParamstyle(spec.paramstyle)
            .addAllNotes(spec.notes)
            .setIdentifiers(
                DialectDescriptor.Identifiers.newBuilder()
                    .setQuote(spec.identifiers.quote.toProto())
                    .setAliasQuote(spec.identifiers.aliasQuote.toProto())
                    .setEscapeQuote(spec.identifiers.escapeQuote)
                    .setUnquotedStorage(spec.identifiers.unquotedStorage)
                    .setQuotedStorage(spec.identifiers.quotedStorage)
                    .setSupportsMixedCase(spec.identifiers.supportsMixedCase)
                    .setSupportsMixedCaseQuoted(spec.identifiers.supportsMixedCaseQuoted)
                    .setMaxLength(spec.identifiers.maxLength)
                    .setExtraNameCharacters(spec.identifiers.extraNameCharacters)
                    .setUseFullyQualifiedNames(spec.identifiers.useFullyQualifiedNames)
                    .build()
            )
            .setCatalogSchema(
                DialectDescriptor.CatalogSchema.newBuilder()
                    .setSupportsSchemas(spec.catalogSchema.supportsSchemas)
                    .setSupportsCatalogs(spec.catalogSchema.supportsCatalogs)
                    .setCatalogSeparator(spec.catalogSchema.catalogSeparator)
                    .setCatalogAtStart(spec.catalogSchema.catalogAtStart)
                    .setSchemaTerm(spec.catalogSchema.schemaTerm)
                    .setCatalogTerm(spec.catalogSchema.catalogTerm)
                    .setProcedureTerm(spec.catalogSchema.procedureTerm)
                    .setSchemasInDml(spec.catalogSchema.schemasInDml)
                    .setSchemasInProcedureCalls(spec.catalogSchema.schemasInProcedureCalls)
                    .setSchemasInTableDefinitions(spec.catalogSchema.schemasInTableDefinitions)
                    .setSchemasInIndexDefinitions(spec.catalogSchema.schemasInIndexDefinitions)
                    .setSchemasInPrivilegeDefinitions(spec.catalogSchema.schemasInPrivilegeDefinitions)
                    .setCatalogsInDml(spec.catalogSchema.catalogsInDml)
                    .setCatalogsInProcedureCalls(spec.catalogSchema.catalogsInProcedureCalls)
                    .setCatalogsInTableDefinitions(spec.catalogSchema.catalogsInTableDefinitions)
                    .setCatalogsInIndexDefinitions(spec.catalogSchema.catalogsInIndexDefinitions)
                    .setCatalogsInPrivilegeDefinitions(spec.catalogSchema.catalogsInPrivilegeDefinitions)
                    .build()
            )
            .setTransactions(
                DialectDescriptor.Transactions.newBuilder()
                    .setSupported(spec.transactions.supported)
                    .setDefaultIsolation(spec.transactions.defaultIsolation)
                    .setSupportsMultiple(spec.transactions.supportsMultiple)
                    .setSupportsDdlAndDml(spec.transactions.supportsDdlAndDml)
                    .setSupportsDmlOnly(spec.transactions.supportsDmlOnly)
                    .setDdlCausesCommit(spec.transactions.ddlCausesCommit)
                    .setDdlIgnoredInTransactions(spec.transactions.ddlIgnoredInTransactions)
                    .build()
            )
            .setLimits(
                DialectDescriptor.Limits.newBuilder()
                    .setMaxBinaryLiteralLength(spec.limits.maxBinaryLiteralLength)
                    .setMaxCharLiteralLength(spec.limits.maxCharLiteralLength)
                    .setMaxColumnNameLength(spec.limits.maxColumnNameLength)
                    .setMaxColumnsInGroupBy(spec.limits.maxColumnsInGroupBy)
                    .setMaxColumnsInIndex(spec.limits.maxColumnsInIndex)
                    .setMaxColumnsInOrderBy(spec.limits.maxColumnsInOrderBy)
                    .setMaxColumnsInSelect(spec.limits.maxColumnsInSelect)
                    .setMaxColumnsInTable(spec.limits.maxColumnsInTable)
                    .setMaxConnections(spec.limits.maxConnections)
                    .setMaxIndexLength(spec.limits.maxIndexLength)
                    .setMaxSchemaNameLength(spec.limits.maxSchemaNameLength)
                    .setMaxCatalogNameLength(spec.limits.maxCatalogNameLength)
                    .setMaxRowSize(spec.limits.maxRowSize)
                    .setMaxRowSizeIncludesBlobs(spec.limits.maxRowSizeIncludesBlobs)
                    .setMaxStatementLength(spec.limits.maxStatementLength)
                    .setMaxStatements(spec.limits.maxStatements)
                    .setMaxTableNameLength(spec.limits.maxTableNameLength)
                    .setMaxTablesInSelect(spec.limits.maxTablesInSelect)
                    .build()
            )
            .setNullSorting(
                DialectDescriptor.NullSorting.newBuilder()
                    .setNullsSortedHigh(spec.nullSorting.nullsSortedHigh)
                    .setNullsSortedLow(spec.nullSorting.nullsSortedLow)
                    .setNullsSortedAtStart(spec.nullSorting.nullsSortedAtStart)
                    .setNullsSortedAtEnd(spec.nullSorting.nullsSortedAtEnd)
                    .setSupportsNullsFirst(spec.nullSorting.supportsNullsFirst)
                    .setSupportsNullsLast(spec.nullSorting.supportsNullsLast)
                    .build()
            )
            .setResultSet(
                DialectDescriptor.ResultSetCaps.newBuilder()
                    .setForwardOnly(spec.resultSet.forwardOnly)
                    .setScrollInsensitive(spec.resultSet.scrollInsensitive)
                    .setScrollSensitive(spec.resultSet.scrollSensitive)
                    .setConcurrencyReadOnly(spec.resultSet.concurrencyReadOnly)
                    .setConcurrencyUpdatable(spec.resultSet.concurrencyUpdatable)
                    .build()
            )
            .putAllFeatureFlags(spec.featureFlags.filterValues { it != null }.mapValues { it.value ?: false })
            .setStringProperties(
                DialectDescriptor.StringProperties.newBuilder()
                    .setSearchStringEscape(spec.stringProperties.searchStringEscape)
                    .setSqlKeywords(spec.stringProperties.sqlKeywords)
                    .setSystemFunctions(spec.stringProperties.systemFunctions)
                    .build()
            )
            .setLiterals(
                DialectDescriptor.Literals.newBuilder()
                    .setStrings(
                        DialectDescriptor.StringLiterals.newBuilder()
                            .setQuote(spec.literals.strings.quote)
                            .setConcat(spec.literals.strings.concat)
                            .setEscape(spec.literals.strings.escape)
                            .setNote(spec.literals.strings.note.orEmpty())
                            .build()
                    )
                    .addAllBooleans(spec.literals.booleans)
                    .setNullLiteral(spec.literals.nullLiteral)
                    .setDatesTimes(
                        DialectDescriptor.DatesTimesLiterals.newBuilder()
                            .setDate(spec.literals.datesTimes.date.toProto())
                            .setTime(spec.literals.datesTimes.time.toProto())
                            .setTimestamp(spec.literals.datesTimes.timestamp.toProto())
                            .setInterval(
                                DialectDescriptor.IntervalLiteral.newBuilder()
                                    .setSupported(spec.literals.datesTimes.interval.supported)
                                    .setStyle(spec.literals.datesTimes.interval.style)
                                    .addAllNotes(spec.literals.datesTimes.interval.notes)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .setJoins(
                DialectDescriptor.Joins.newBuilder()
                    .setStyle(spec.joins.style)
                    .setCrossJoin(spec.joins.crossJoin.toProto())
                    .setInnerJoin(spec.joins.innerJoin.toProto())
                    .setLeftJoin(spec.joins.leftJoin.toProto())
                    .setRightJoin(spec.joins.rightJoin.toProto())
                    .setFullJoin(spec.joins.fullJoin.toProto())
                    .setOnClause(
                        DialectDescriptor.OnClause.newBuilder()
                            .setKeyword(spec.joins.onClause.keyword)
                            .setRequireCondition(spec.joins.onClause.requireCondition)
                            .build()
                    )
                    .build()
            )
            .setPaging(
                DialectDescriptor.Paging.newBuilder()
                    .addAllStyles(
                        spec.paging.styles.map {
                            DialectDescriptor.PagingStyle.newBuilder()
                                .setSyntax(it.syntax)
                                .setType(it.type)
                                .setDeprecated(it.deprecated ?: false)
                                .build()
                        }
                    )
                    .setOffset(spec.paging.offset)
                    .apply { spec.paging.noLimitValue?.let { setNoLimitValue(it) } }
                    .build()
            )
            .putAllOperators(
                spec.operators.mapValues { (_, entries) ->
                    DialectDescriptor.OperatorCategory.newBuilder()
                        .addAllEntries(
                            entries.map { e ->
                                DialectDescriptor.OperatorEntry.newBuilder()
                                    .setSymbol(e.symbol)
                                    .apply {
                                        e.syntax?.let { setSyntax(it) }
                                        e.description?.let { setDescription(it) }
                                        e.supported?.let { setSupported(it) }
                                        e.deprecated?.let { setDeprecated(it) }
                                    }
                                    .build()
                            }
                        )
                        .build()
                }
            )
            .putAllFunctions(
                spec.functions.mapValues { (_, entries) ->
                    DialectDescriptor.FunctionCategory.newBuilder()
                        .addAllEntries(entries.map { it.toProto() })
                        .build()
                }
            )
            .addAllTypeInfo(spec.typeInfo.map { it.toProto() })
            .build()

    private fun QuotePair.toProto(): DialectDescriptor.QuotePair =
        DialectDescriptor.QuotePair.newBuilder()
            .setStart(start)
            .setEnd(end)
            .build()

    private fun DateTimeLiteral.toProto(): DialectDescriptor.DateTimeLiteral =
        DialectDescriptor.DateTimeLiteral.newBuilder()
            .setSyntax(syntax)
            .setQuote(quote)
            .setPattern(pattern)
            .addAllNotes(notes)
            .build()

    private fun JoinType.toProto(): DialectDescriptor.JoinType =
        DialectDescriptor.JoinType.newBuilder()
            .apply {
                enabled?.let { setEnabled(it) }
                keyword?.let { setKeyword(it) }
                requireOn?.let { setRequireOn(it) }
                nullSafe?.let { setNullSafe(it) }
                notes?.let { setNotes(it) }
            }
            .build()

    private fun FunctionEntry.toProto(): DialectDescriptor.FunctionEntry =
        DialectDescriptor.FunctionEntry.newBuilder()
            .setName(name)
            .addAllSynonyms(synonyms)
            .setReturnType(
                DialectDescriptor.ReturnType.newBuilder()
                    .setType(returnType.type)
                    .setNullable(returnType.nullable)
                    .build()
            )
            .setSyntax(syntax)
            .addAllArgs(args.map { it.toProto() })
            .addAllNotes(notes)
            .build()

    private fun FunctionArg.toProto(): DialectDescriptor.FunctionArg =
        DialectDescriptor.FunctionArg.newBuilder()
            .setName(name)
            .setType(type)
            .setRequired(required)
            .apply {
                variadic?.let { setVariadic(it) }
                multi?.let { setMulti(it) }
                min?.let { setMin(it) }
                max?.let { setMax(it) }
                `enum`?.let { addAllEnumValues(it) }
                defaultValue?.let { setDefaultValue(it) }
                notes?.let { setNotes(it) }
            }
            .build()

    private fun TypeInfo.toProto(): DialectDescriptor.TypeInfo =
        DialectDescriptor.TypeInfo.newBuilder()
            .setSqlName(sqlName)
            .setJdbcTypeCode(jdbcTypeCode)
            .apply {
                precision?.let { setPrecision(it) }
                literalPrefix?.let { setLiteralPrefix(it) }
                literalSuffix?.let { setLiteralSuffix(it) }
                caseSensitive?.let { setCaseSensitive(it) }
                searchable?.let { setSearchable(it) }
                unsigned?.let { setUnsigned(it) }
                fixedPrecScale?.let { setFixedPrecScale(it) }
                autoIncrement?.let { setAutoIncrement(it) }
                minimumScale?.let { setMinimumScale(it) }
                maximumScale?.let { setMaximumScale(it) }
                numPrecRadix?.let { setNumPrecRadix(it) }
            }
            .build()
}
