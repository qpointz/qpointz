package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SourceTable
import io.qpointz.mill.types.sql.DatabaseType
import io.qpointz.mill.vectors.VectorBlockIterator
import org.apache.calcite.DataContext
import org.apache.calcite.jdbc.JavaTypeFactoryImpl
import org.apache.calcite.sql.type.SqlTypeName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class FlowTableTest {

    private val schema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, 100),
        "active" to DatabaseType.bool(false)
    )

    private val records = listOf(
        Record.of("id" to 1, "name" to "Alice", "active" to true),
        Record.of("id" to 2, "name" to "Bob", "active" to false),
        Record.of("id" to 3, "name" to null, "active" to true)
    )

    private fun stubSourceTable(
        schema: RecordSchema = this.schema,
        records: List<Record> = this.records
    ): SourceTable = object : SourceTable {
        override val schema: RecordSchema = schema
        override fun records(): Iterable<Record> = records
        override fun vectorBlocks(batchSize: Int): VectorBlockIterator {
            throw UnsupportedOperationException("not used in scan tests")
        }
    }

    private val mockDataContext: DataContext = Mockito.mock(DataContext::class.java)

    @Test
    fun shouldReturnCorrectRowType() {
        val table = FlowTable(stubSourceTable())
        val typeFactory = JavaTypeFactoryImpl()
        val rowType = table.getRowType(typeFactory)

        assertEquals(3, rowType.fieldCount)
        assertEquals("id", rowType.fieldList[0].name)
        assertEquals(SqlTypeName.INTEGER, rowType.fieldList[0].type.sqlTypeName)
        assertEquals("name", rowType.fieldList[1].name)
        assertEquals(SqlTypeName.VARCHAR, rowType.fieldList[1].type.sqlTypeName)
        assertEquals("active", rowType.fieldList[2].name)
        assertEquals(SqlTypeName.BOOLEAN, rowType.fieldList[2].type.sqlTypeName)
    }

    @Test
    fun shouldScanAllRecords() {
        val table = FlowTable(stubSourceTable())
        val enumerable = table.scan(mockDataContext)
        val enumerator = enumerable.enumerator()

        val result = mutableListOf<Array<Any?>>()
        while (enumerator.moveNext()) {
            result.add(enumerator.current().clone())
        }
        enumerator.close()

        assertEquals(3, result.size)

        // Row 0: id=1, name=Alice, active=true
        assertEquals(1, result[0][0])
        assertEquals("Alice", result[0][1])
        assertEquals(true, result[0][2])

        // Row 1: id=2, name=Bob, active=false
        assertEquals(2, result[1][0])
        assertEquals("Bob", result[1][1])
        assertEquals(false, result[1][2])

        // Row 2: id=3, name=null, active=true
        assertEquals(3, result[2][0])
        assertNull(result[2][1])
        assertEquals(true, result[2][2])
    }

    @Test
    fun shouldScanEmptyTable() {
        val table = FlowTable(stubSourceTable(records = emptyList()))
        val enumerable = table.scan(mockDataContext)
        val enumerator = enumerable.enumerator()

        assertFalse(enumerator.moveNext())
        enumerator.close()
    }

    @Test
    fun shouldProjectFieldsInSchemaOrder() {
        // Ensure that even if Record map has different insertion order,
        // the scan output follows schema field order
        val customRecords = listOf(
            Record(mapOf("active" to true, "id" to 42, "name" to "Zara"))
        )
        val table = FlowTable(stubSourceTable(records = customRecords))
        val enumerable = table.scan(mockDataContext)
        val enumerator = enumerable.enumerator()

        assertTrue(enumerator.moveNext())
        val row = enumerator.current()
        // Schema order: id, name, active
        assertEquals(42, row[0])
        assertEquals("Zara", row[1])
        assertEquals(true, row[2])

        assertFalse(enumerator.moveNext())
        enumerator.close()
    }
}
