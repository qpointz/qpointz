package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.parcels.ParcelTable;
import io.quarkus.arc.All;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;

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
        this.parentSchema.getTablePaths(this.name);
        return null;
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root) {
        return null;
    }
}
