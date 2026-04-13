package io.qpointz.mill.ai.data.sql

import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidationOutcome
import io.qpointz.mill.ai.capabilities.sqlquery.SqlValidator
import io.qpointz.mill.data.schema.SchemaFacetService
import io.qpointz.mill.data.schema.SchemaWithFacets
import org.apache.calcite.sql.SqlCall
import org.apache.calcite.sql.SqlIdentifier
import org.apache.calcite.sql.SqlJoin
import org.apache.calcite.sql.SqlKind
import org.apache.calcite.sql.SqlNode
import org.apache.calcite.sql.SqlOrderBy
import org.apache.calcite.sql.SqlSelect
import org.apache.calcite.sql.SqlWith
import org.apache.calcite.sql.parser.SqlParser

/**
 * [SqlValidator] that parses SQL with Calcite and, when a [SchemaFacetService] is supplied,
 * checks **FROM** / **JOIN** table references against the merged physical catalog.
 *
 * Thread-safety: this class is immutable and relies on thread-safe collaborators.
 */
class EngineBackedSqlValidator(
    private val schemaFacetService: SchemaFacetService,
) : SqlValidator {

    override fun validate(sql: String): SqlValidationOutcome {
        val trimmed = sql.trim()
        if (trimmed.isEmpty()) {
            return SqlValidationOutcome(false, message = "SQL is blank")
        }
        val root = try {
            SqlParser.create(trimmed, SqlParser.Config.DEFAULT).parseQuery()
        } catch (ex: Exception) {
            return SqlValidationOutcome(false, message = ex.message ?: "SQL parse error")
        }
        val select = unwrapToSelect(root)
            ?: return SqlValidationOutcome(
                false,
                message = "Only supported SELECT-shaped statements can be validated",
            )
        return try {
            validateFromClause(schemaFacetService, select.from)
            SqlValidationOutcome(passed = true, message = null, normalizedSql = trimmed)
        } catch (ex: IllegalStateException) {
            SqlValidationOutcome(false, message = ex.message)
        }
    }

    private fun unwrapToSelect(node: SqlNode): SqlSelect? {
        var n: SqlNode? = node
        while (n != null && n !is SqlSelect) {
            n = when (n) {
                is SqlOrderBy -> n.query
                is SqlWith -> n.body
                else -> return null
            }
        }
        return n as? SqlSelect
    }

    private fun validateFromClause(facet: SchemaFacetService, from: SqlNode?) {
        if (from == null) return
        when (from) {
            is SqlJoin -> {
                validateFromClause(facet, from.left)
                validateFromClause(facet, from.right)
            }
            is SqlCall -> when (from.kind) {
                SqlKind.AS -> validateFromClause(facet, from.operand(0))
                else -> Unit
            }
            is SqlSelect -> validateFromClause(facet, from.from)
            is SqlIdentifier -> resolveTable(facet, from)
            else -> Unit
        }
    }

    private fun resolveTable(facet: SchemaFacetService, id: SqlIdentifier) {
        val names = id.names ?: return
        if (names.size < 2) return
        val schemaName = names[names.size - 2]
        val tableName = names[names.size - 1]
        val schemas: List<SchemaWithFacets> = facet.getSchemas().schemas
        val schema = schemas.firstOrNull { it.schemaName == schemaName }
            ?: schemas.firstOrNull { it.schemaName.equals(schemaName, ignoreCase = true) }
            ?: throw IllegalStateException("Unknown schema: $schemaName")
        val table = schema.tables.firstOrNull { it.tableName == tableName }
            ?: schema.tables.firstOrNull { it.tableName.equals(tableName, ignoreCase = true) }
            ?: throw IllegalStateException("Unknown table: $schemaName.$tableName")
    }
}
