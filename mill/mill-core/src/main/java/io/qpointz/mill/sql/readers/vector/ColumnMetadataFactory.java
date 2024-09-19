package io.qpointz.mill.sql.readers.vector;

import io.qpointz.mill.proto.Field;
import io.qpointz.mill.types.logical.LogicalTypeIdMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

@AllArgsConstructor
public class ColumnMetadataFactory extends LogicalTypeIdMapper<ColumnMetadata> {

    @Getter
    private final Field field;



    private ColumnMetadata.ColumnMetadataBuilder buildCommon() {
        val type = field.getType();
        val logType = type.getType();
        return ColumnMetadata.builder()
                .name(field.getName())
                .isNullable(type.getNullability())
                .scale(logType.getScale())
                .precision(logType.getPrecision())
                .displaySize(logType.getScale())
                .isSigned(false)
                .logicalTypeId(logType.getTypeId());

    }


    @Override
    protected ColumnMetadata mapUUID() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapTime() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapTimestampTZ() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapTimestamp() {
            return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapString() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapIntervalYear() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapIntervalDay() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapDouble() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapFloat() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapDate() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapBool() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapBinary() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapInt() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapSmallInt() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapTinyInt() {
        return buildCommon().build();
    }

    @Override
    protected ColumnMetadata mapBigInt() {
        return buildCommon().build();
    }
}
