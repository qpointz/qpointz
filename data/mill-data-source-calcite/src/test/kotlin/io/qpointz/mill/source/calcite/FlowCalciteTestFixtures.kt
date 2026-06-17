package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.BlobSource
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.ResolvedSource
import io.qpointz.mill.source.SourceTable
import io.qpointz.mill.source.descriptor.ConflictResolution
import io.qpointz.mill.source.factory.MaterializedSource
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import org.apache.calcite.jdbc.CalciteConnection
import org.apache.calcite.schema.SchemaPlus
import org.apache.calcite.tools.Frameworks
import java.io.InputStream
import java.nio.channels.SeekableByteChannel
import java.sql.DriverManager
import java.util.Properties

/**
 * Shared fixtures for Calcite + [FlowSchema] integration tests.
 */
object FlowCalciteTestFixtures {

    const val SCHEMA_NAME = "demo"

    val USERS_SCHEMA: RecordSchema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, 100),
        "active" to DatabaseType.bool(false),
    )

    val USERS_RECORDS: List<Record> = listOf(
        Record.of("id" to 1, "name" to "Alice", "active" to true),
        Record.of("id" to 2, "name" to "Bob", "active" to false),
        Record.of("id" to 3, "name" to "Carol", "active" to true),
    )

    /**
     * In-memory [SourceTable] with configurable schema and rows.
     *
     * @param schema table schema
     * @param records rows to expose from [SourceTable.records]
     */
    fun inMemorySourceTable(
        schema: RecordSchema = USERS_SCHEMA,
        records: List<Record> = USERS_RECORDS,
    ): SourceTable = object : SourceTable {
        override val schema: RecordSchema = schema
        override fun records(): Iterable<Record> = records
        override fun vectorBlocks(batchSize: Int): VectorBlockIterator {
            throw UnsupportedOperationException("vector blocks not used in translatable-table tests")
        }
    }

    /**
     * Builds a [ResolvedSource] with the given logical tables.
     *
     * @param tables map of table name to [SourceTable]
     */
    fun resolvedSource(tables: Map<String, SourceTable>): ResolvedSource {
        val materialized = MaterializedSource(
            name = SCHEMA_NAME,
            blobSource = stubBlobSource(),
            readers = emptyList(),
            conflicts = ConflictResolution.DEFAULT,
        )
        return ResolvedSource(materialized, tables)
    }

    /**
     * Opens a Calcite JDBC connection with [schemaName] registered as a [FlowSchema].
     *
     * @param schemaName Calcite sub-schema name
     * @param tables logical tables exposed by the schema
     * @return connection and root schema (connection must be closed by caller)
     */
    fun openCalciteConnection(
        schemaName: String = SCHEMA_NAME,
        tables: Map<String, SourceTable> = mapOf("users" to inMemorySourceTable()),
    ): Pair<CalciteConnection, SchemaPlus> {
        Class.forName("org.apache.calcite.jdbc.Driver")
        val connection = DriverManager
            .getConnection("jdbc:calcite:", Properties())
            .unwrap(CalciteConnection::class.java)
        val rootSchema = connection.rootSchema
        rootSchema.add(schemaName, FlowSchema(resolvedSource(tables)))
        connection.schema = schemaName
        return connection to rootSchema
    }

    /**
     * Framework config with [schemaName] as the default schema for SQL parsing tests.
     *
     * @param rootSchema Calcite root schema containing the flow sub-schema
     * @param schemaName default schema name
     */
    fun frameworkConfig(rootSchema: SchemaPlus, schemaName: String = SCHEMA_NAME) =
        Frameworks.newConfigBuilder()
            .defaultSchema(rootSchema.getSubSchema(schemaName))
            .build()

    private fun stubBlobSource(): BlobSource = object : BlobSource {
        override fun listBlobs(): Sequence<BlobPath> = emptySequence()
        override fun openInputStream(path: BlobPath): InputStream =
            throw UnsupportedOperationException()
        override fun openSeekableChannel(path: BlobPath): SeekableByteChannel =
            throw UnsupportedOperationException()
        override fun close() {}
    }
}
