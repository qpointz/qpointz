package io.qpointz.rapids.formats.parquet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.AbstractQueryableTable;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.QueryableTable;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.parquet.hadoop.ParquetReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class implements Calcite table backed by parquet files
 */

@AllArgsConstructor
@Slf4j
public class RapidsParquetTable extends AbstractTable implements ScannableTable {

    @Getter
    private final RapidsParquetSchema parentSchema;

    @Getter
    private final String name;


    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        return getRelDataType(typeFactory);
    }

    private RelDataType getRelDataType(RelDataTypeFactory typeFactory) {
        try {
            return this.parentSchema.getRowType(this.name, typeFactory);
        } catch (IOException e) {
             throw new RuntimeException(e);
        }
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        var typeFactory = root.getTypeFactory();
        RelDataType rowType;
        var paths = this.parentSchema.getTablePaths(this.getName());
        try {
            rowType = this.parentSchema.getRowType(this.getName(), typeFactory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var fieldCount = rowType.getFieldCount();
        var fieldList = rowType.getFieldList();
        return new FileEnumerator<GenericRecord>(paths) {

            @Override
            protected Stream<GenericRecord> createRecordIterator(Path k) {
                ParquetReader<GenericRecord> reader;
                try {
                    reader = RapidsParquetTable.this.parentSchema.getParquetReader(k);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return Stream.generate(()-> {
                    try {
                        return reader.read();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).takeWhile(Objects::nonNull);
            }

            @Override
            Object[] mapRecord(GenericRecord genericRecord) {
                var r = new Object[fieldCount];
                for (int i=0;i<fieldCount;i++) {
                    var f = fieldList.get(i);
                    r[i] = genericRecord.get(f.getName());
                }
                return r;
            }
        };
    }
}

