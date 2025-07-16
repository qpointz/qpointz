package io.qpointz.mill.types.sql;

public class JdbcDatabaseTypeMapper extends JdbcTypeMapper<DatabaseType> {

    public static final JdbcDatabaseTypeMapper DEFAULT = new JdbcDatabaseTypeMapper();

    @Override
    protected DatabaseType mapBoolean(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.bool(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapBit(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.bool(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapTinyInt(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.i16(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapInt(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.i32(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapSmallInt(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.i32(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapBigInt(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.i64(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapFloat(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.fp32(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec(), jdbcTypeInfo.scale());
    }

    @Override
    protected DatabaseType mapDouble(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.fp64(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec(), jdbcTypeInfo.scale());
    }

    @Override
    protected DatabaseType mapReal(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.fp64(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec(), jdbcTypeInfo.scale());
    }

    @Override
    protected DatabaseType mapNumeric(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.fp64(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec(), jdbcTypeInfo.scale());
    }

    @Override
    protected DatabaseType mapDecimal(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.fp64(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec(), jdbcTypeInfo.scale());
    }

    @Override
    protected DatabaseType mapNVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapChar(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapNChar(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapLongVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapLongNVarChar(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapNClob(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapClob(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.string(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapBinary(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.binary(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapVarBinary(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.binary(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapLongVarBinary(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.binary(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapBlob(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.binary(jdbcTypeInfo.nullable(), jdbcTypeInfo.prec());
    }

    @Override
    protected DatabaseType mapDate(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.date(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapTime(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.time(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapTimestamp(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.timetz(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapTimeWithTZ(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.timetz(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapTimestampWithTZ(JdbcTypeInfo jdbcTypeInfo) {
        return DatabaseType.timetz(jdbcTypeInfo.nullable());
    }

    @Override
    protected DatabaseType mapNull(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapOther(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapJavaObject(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapDistinct(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapStruct(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapArray(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapRef(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapDataLink(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapRowId(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapSqlXML(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

    @Override
    protected DatabaseType mapRefCursor(JdbcTypeInfo jdbcTypeInfo) {
        throwNotSupported(jdbcTypeInfo);
        return null;
    }

}
