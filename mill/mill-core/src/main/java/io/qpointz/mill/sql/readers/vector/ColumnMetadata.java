package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.proto.LogicalDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public class ColumnMetadata {

    @Getter
    private final DataType.Nullability isNullable;

    @Getter
    private final boolean isSigned;

    @Getter
    private final int displaySize;

    @Getter
    private final String name;

    @Getter
    private final int precision;

    @Getter
    private final int scale;

    @Getter
    private final String typeName;

    @Getter
    private final String className;

    public boolean isAutoIncrement() {
        return false;
    }

    public boolean isCaseSensitive() {
        return false;
    }

    public boolean isSearchable() {
        return true;
    }

    public boolean isCurrency() {
        return false;
    }

    @Getter
    private final LogicalDataType.LogicalDataTypeId logicalTypeId;
}
