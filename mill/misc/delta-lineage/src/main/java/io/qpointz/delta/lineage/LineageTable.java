package io.qpointz.mill.lineage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.ddl.SqlColumnDeclaration;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Pair;

import java.util.List;

@AllArgsConstructor
public class LineageTable extends AbstractTable {

    @Getter
    SqlIdentifier tableName;

    @Getter
    List<SqlColumnDeclaration> columnList;

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        val mapped = columnList.stream().map(k-> Pair.of(
                k.name.toString(),
                typeFactory.createSqlType(SqlTypeName.get(k.dataType.getTypeName().toString()))
                )).toList();

        val names = mapped.stream().map(k-> k.left).toList();
        val types = mapped.stream().map(k -> k.right).toList();
        return typeFactory.createStructType(types, names);
    }
}
