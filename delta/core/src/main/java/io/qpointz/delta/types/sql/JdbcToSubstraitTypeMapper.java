package io.qpointz.delta.types.sql;

import io.substrait.proto.Type;

public class JdbcToSubstraitTypeMapper extends JdbcTypeMapper<io.substrait.proto.Type> {

    public static JdbcToSubstraitTypeMapper DEFAULT = new JdbcToSubstraitTypeMapper();

    private Type.Nullability getNullability(JdbcTypeInfo jdbcTypeInfo) {
        return jdbcTypeInfo.nullable() ? Type.Nullability.NULLABILITY_NULLABLE
                : Type.Nullability.NULLABILITY_REQUIRED;
    }

    @Override
    protected Type mapBoolean(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setBool(Type.Boolean.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapBit(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setBool(Type.Boolean.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapTinyInt(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setI8(Type.I8.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapInt(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setI32(Type.I32.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapSmallInt(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setI16(Type.I16.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapBigInt(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setI64(Type.I64.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapFloat(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setFp32(Type.FP32.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapDouble(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setFp64(Type.FP64.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapReal(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setFp64(Type.FP64.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapNumeric(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setFp64(Type.FP64.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapDecimal(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setFp64(Type.FP64.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapNVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setVarchar(Type.VarChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setVarchar(Type.VarChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapChar(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setFixedChar(Type.FixedChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapNChar(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setFixedChar(Type.FixedChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapLongVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setVarchar(Type.VarChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapLongNVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setVarchar(Type.VarChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapNClob(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setVarchar(Type.VarChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapClob(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setVarchar(Type.VarChar.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo))
                        .setLength(jdbcTypeInfo.prec()))
                .build();
    }

    @Override
    protected Type mapBinary(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setBinary(Type.Binary.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapVarBinary(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setBinary(Type.Binary.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapLongVarBinary(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setBinary(Type.Binary.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapBlob(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setBinary(Type.Binary.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapDate(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setDate(Type.Date.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapTime(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setTime(Type.Time.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapTimestamp(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setPrecisionTimestamp(Type.PrecisionTimestamp.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapTimeWithTZ(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setPrecisionTimestampTz(Type.PrecisionTimestampTZ.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapTimestampWithTZ(JdbcTypeInfo jdbcTypeInfo) {
        return Type.newBuilder().setPrecisionTimestampTz(Type.PrecisionTimestampTZ.newBuilder()
                        .setNullability(getNullability(jdbcTypeInfo)))
                .build();
    }

    @Override
    protected Type mapNull(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapOther(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapJavaObject(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapDistinct(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapStruct(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapArray(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapRef(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapDataLink(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapRowId(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapSqlXML(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected Type mapRefCursor(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }
}
