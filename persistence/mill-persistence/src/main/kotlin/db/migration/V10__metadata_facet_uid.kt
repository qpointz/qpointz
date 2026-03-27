package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.Connection
import java.util.UUID

/**
 * Adds a stable [facet_uid] column to [metadata_facet] for per-instance identity in the REST API
 * (multi-cardinality facet deletes and list responses). Backfills existing rows, then enforces
 * NOT NULL and a unique index.
 */
@Suppress("ClassName", "unused")
class V10__metadata_facet_uid : BaseJavaMigration() {

    override fun migrate(context: Context) {
        val connection = context.connection
        exec(connection, "ALTER TABLE metadata_facet ADD COLUMN IF NOT EXISTS facet_uid VARCHAR(36)")
        backfillUids(connection)
        exec(connection, "ALTER TABLE metadata_facet ALTER COLUMN facet_uid SET NOT NULL")
        exec(connection, "CREATE UNIQUE INDEX IF NOT EXISTS uq_metadata_facet_uid ON metadata_facet (facet_uid)")
    }

    private fun backfillUids(connection: Connection) {
        connection.prepareStatement(
            "SELECT facet_id FROM metadata_facet WHERE facet_uid IS NULL"
        ).use { select ->
            select.executeQuery().use { rs ->
                connection.prepareStatement(
                    "UPDATE metadata_facet SET facet_uid = ? WHERE facet_id = ?"
                ).use { update ->
                    while (rs.next()) {
                        val facetId = rs.getLong(1)
                        update.setString(1, UUID.randomUUID().toString())
                        update.setLong(2, facetId)
                        update.executeUpdate()
                    }
                }
            }
        }
    }

    private fun exec(connection: Connection, sql: String) {
        connection.createStatement().use { it.execute(sql) }
    }
}
