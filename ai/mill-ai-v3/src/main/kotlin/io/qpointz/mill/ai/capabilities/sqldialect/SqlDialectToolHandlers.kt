package io.qpointz.mill.ai.capabilities.sqldialect

import io.qpointz.mill.sql.v2.dialect.JoinType
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec

/**
 * Pure stateless implementations of the five SQL dialect tool handlers.
 *
 * Each function maps a [SqlDialectSpec] to a flat, JSON-serializable result type.
 * The result types mirror the output schemas declared in `capabilities/sql-dialect.yaml` exactly.
 *
 * These handlers do not know anything about [io.qpointz.mill.ai.Capability],
 * [io.qpointz.mill.ai.AgentContext], or [io.qpointz.mill.ai.CapabilityRegistry].
 */
object SqlDialectToolHandlers {

    // ── get_sql_dialect_conventions ───────────────────────────────────────────

    data class IdentifierConventions(
        val quoteStart: String,
        val quoteEnd: String,
        val escapeQuote: String,
        val unquotedStorage: String,
        val quotedStorage: String,
        val supportsMixedCase: Boolean,
        val supportsMixedCaseQuoted: Boolean,
        val maxLength: Int,
        val useFullyQualifiedNames: Boolean,
    )

    data class LiteralConventions(
        val stringQuote: String,
        val stringConcat: String,
        val stringEscape: String,
        val nullLiteral: String,
        val booleanLiterals: List<String>,
    )

    data class SqlDialectConventions(
        val dialectId: String,
        val dialectName: String,
        val notes: List<String>,
        val identifiers: IdentifierConventions,
        val literals: LiteralConventions,
        val functionCategories: List<String>,
    )

    fun getSqlDialectConventions(spec: SqlDialectSpec): SqlDialectConventions =
        SqlDialectConventions(
            dialectId = spec.id,
            dialectName = spec.name,
            notes = spec.notes,
            identifiers = IdentifierConventions(
                quoteStart = spec.identifiers.quote.start,
                quoteEnd = spec.identifiers.quote.end,
                escapeQuote = spec.identifiers.escapeQuote,
                unquotedStorage = spec.identifiers.unquotedStorage,
                quotedStorage = spec.identifiers.quotedStorage,
                supportsMixedCase = spec.identifiers.supportsMixedCase,
                supportsMixedCaseQuoted = spec.identifiers.supportsMixedCaseQuoted,
                maxLength = spec.identifiers.maxLength,
                useFullyQualifiedNames = spec.identifiers.useFullyQualifiedNames,
            ),
            literals = LiteralConventions(
                stringQuote = spec.literals.strings.quote,
                stringConcat = spec.literals.strings.concat,
                stringEscape = spec.literals.strings.escape,
                nullLiteral = spec.literals.nullLiteral,
                booleanLiterals = spec.literals.booleans,
            ),
            functionCategories = spec.functions.keys.toList().sorted(),
        )

    // ── get_sql_paging_rules ──────────────────────────────────────────────────

    data class PagingStyleItem(
        val syntax: String,
        val type: String,
        val deprecated: Boolean,
    )

    data class SqlPagingRules(
        val styles: List<PagingStyleItem>,
        val offset: String,
        val noLimitValue: String?,
    )

    fun getSqlPagingRules(spec: SqlDialectSpec): SqlPagingRules =
        SqlPagingRules(
            styles = spec.paging.styles.map {
                PagingStyleItem(it.syntax, it.type, it.deprecated ?: false)
            },
            offset = spec.paging.offset,
            noLimitValue = spec.paging.noLimitValue,
        )

    // ── get_sql_join_rules ────────────────────────────────────────────────────

    data class JoinTypeInfo(
        val name: String,
        val enabled: Boolean,
        val keyword: String?,
        val requireOn: Boolean?,
        val nullSafe: Boolean?,
        val notes: String?,
    )

    data class SqlJoinRules(
        val style: String,
        val onClauseKeyword: String,
        val onClauseRequireCondition: Boolean,
        val joinTypes: List<JoinTypeInfo>,
    )

    fun getSqlJoinRules(spec: SqlDialectSpec): SqlJoinRules {
        fun info(name: String, jt: JoinType) = JoinTypeInfo(
            name = name,
            enabled = jt.enabled ?: true,
            keyword = jt.keyword,
            requireOn = jt.requireOn,
            nullSafe = jt.nullSafe,
            notes = jt.notes,
        )
        return SqlJoinRules(
            style = spec.joins.style,
            onClauseKeyword = spec.joins.onClause.keyword,
            onClauseRequireCondition = spec.joins.onClause.requireCondition,
            joinTypes = listOf(
                info("CROSS", spec.joins.crossJoin),
                info("INNER", spec.joins.innerJoin),
                info("LEFT",  spec.joins.leftJoin),
                info("RIGHT", spec.joins.rightJoin),
                info("FULL",  spec.joins.fullJoin),
            ),
        )
    }

    // ── get_sql_functions ─────────────────────────────────────────────────────

    data class FunctionSummary(
        val name: String,
        val description: String?,
        val synonyms: List<String>,
    )

    data class SqlFunctionsResult(
        val category: String,
        val functions: List<FunctionSummary>,
    )

    data class UnknownCategoryResult(
        val error: String,
        val availableCategories: List<String>,
    )

    fun getSqlFunctions(spec: SqlDialectSpec, category: String): Any {
        val entries = spec.functions[category]
            ?: return UnknownCategoryResult(
                error = "Unknown function category: $category",
                availableCategories = spec.functions.keys.toList().sorted(),
            )
        return SqlFunctionsResult(
            category = category,
            functions = entries.map { FunctionSummary(it.name, it.notes.firstOrNull(), it.synonyms) },
        )
    }

    // ── get_sql_function_info ─────────────────────────────────────────────────

    data class FunctionArgInfo(
        val name: String,
        val type: String,
        val required: Boolean,
        val variadic: Boolean?,
        val multi: Boolean?,
        val enum: List<String>?,
        val defaultValue: String?,
        val notes: String?,
    )

    data class FunctionInfo(
        val name: String,
        val category: String,
        val synonyms: List<String>,
        val syntax: String,
        val returnType: String,
        val returnNullable: Boolean,
        val args: List<FunctionArgInfo>,
        val notes: List<String>,
    )

    data class UnknownFunctionResult(
        val error: String,
    )

    fun getSqlFunctionInfo(spec: SqlDialectSpec, name: String): Any {
        for ((category, entries) in spec.functions) {
            val entry = entries.find { entry ->
                entry.name == name || entry.synonyms.any { it == name }
            }
            if (entry != null) {
                return FunctionInfo(
                    name = entry.name,
                    category = category,
                    synonyms = entry.synonyms,
                    syntax = entry.syntax,
                    returnType = entry.returnType.type,
                    returnNullable = entry.returnType.nullable,
                    args = entry.args.map { arg ->
                        FunctionArgInfo(
                            name = arg.name,
                            type = arg.type,
                            required = arg.required,
                            variadic = arg.variadic,
                            multi = arg.multi,
                            enum = arg.`enum`,
                            defaultValue = arg.defaultValue,
                            notes = arg.notes,
                        )
                    },
                    notes = entry.notes,
                )
            }
        }
        return UnknownFunctionResult(error = "Unknown function: $name")
    }
}
