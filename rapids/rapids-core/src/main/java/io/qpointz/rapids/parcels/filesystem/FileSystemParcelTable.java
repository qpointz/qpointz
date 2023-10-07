package io.qpointz.rapids.parcels.filesystem;

import io.qpointz.rapids.parcels.ParcelTable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.type.SqlTypeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class FileSystemParcelTable extends ParcelTable {

    @Getter
    private final FileSystemParcel parentParcel;

    @Getter
    private static final boolean exposePartitions = true;

    public FileSystemParcelTable(FileSystemParcel parentParcel) {
        this.parentParcel = parentParcel;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        var types = new ArrayList<RelDataType>();
        var names = new ArrayList<String>();
        this.addPartitions(types, names, typeFactory);
        return typeFactory.createStructType(types, names);
    }

    private void addPartitions(Collection<RelDataType> types, Collection<String> names, RelDataTypeFactory typeFactory) {
        if (! this.exposePartitions) {
            return;
        }

        var partitions = this.getParentParcel().getPartitionMatcher().getPartitionValueGroups();
        for (var pd : partitions.entrySet()) {
            names.add(String.format("_PART_%s", pd.getKey()));
            types.add(switch (pd.getValue()) {
                case STRING -> typeFactory.createSqlType(SqlTypeName.VARCHAR);
                case INT -> typeFactory.createSqlType(SqlTypeName.INTEGER);
            });
        }
    }

    @Override
    public Enumerable<Object[]> scan(DataContext root, List<RexNode> filters) {
        return null;
    }
}
