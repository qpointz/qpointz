package io.qpointz.mill.types.sql;

public abstract class JdbcTypeMapper<E> {

    public E jdbc(JdbcTypeInfo jdbcTypeInfo) {
        return switch (jdbcTypeInfo.getJdbcType()) {
            case BOOLEAN        -> mapBoolean(jdbcTypeInfo);
            case BIT            -> mapBit(jdbcTypeInfo);
            case TINYINT        -> mapTinyInt(jdbcTypeInfo);
            case INTEGER        -> mapInt(jdbcTypeInfo);
            case SMALLINT       -> mapSmallInt(jdbcTypeInfo);
            case BIGINT         -> mapBigInt(jdbcTypeInfo);
            case FLOAT          -> mapFloat(jdbcTypeInfo);
            case DOUBLE         -> mapDouble(jdbcTypeInfo);
            case REAL           -> mapReal(jdbcTypeInfo);
            case NUMERIC        -> mapNumeric(jdbcTypeInfo);
            case DECIMAL        -> mapDecimal(jdbcTypeInfo);
            case NVARCHAR       -> mapNVarChar(jdbcTypeInfo);
            case VARCHAR        -> mapVarChar(jdbcTypeInfo);
            case CHAR           -> mapChar(jdbcTypeInfo);
            case NCHAR          -> mapNChar(jdbcTypeInfo);
            case LONGVARCHAR    -> mapLongVarChar(jdbcTypeInfo);
            case LONGNVARCHAR   -> mapLongNVarChar(jdbcTypeInfo);
            case NCLOB          -> mapNClob(jdbcTypeInfo);
            case CLOB           -> mapClob(jdbcTypeInfo);

            case BINARY         -> mapBinary(jdbcTypeInfo);
            case VARBINARY      -> mapVarBinary(jdbcTypeInfo);
            case LONGVARBINARY  -> mapLongVarBinary(jdbcTypeInfo);
            case BLOB           -> mapBlob(jdbcTypeInfo);

            case DATE           -> mapDate(jdbcTypeInfo);
            case TIME           -> mapTime(jdbcTypeInfo);
            case TIMESTAMP      -> mapTimestamp(jdbcTypeInfo);
            case TIME_WITH_TIMEZONE       -> mapTimeWithTZ(jdbcTypeInfo);
            case TIMESTAMP_WITH_TIMEZONE  -> mapTimestampWithTZ(jdbcTypeInfo);

            case NULL           -> mapNull(jdbcTypeInfo);
            case OTHER          -> mapOther(jdbcTypeInfo);
            case JAVA_OBJECT    -> mapJavaObject(jdbcTypeInfo);
            case DISTINCT       -> mapDistinct(jdbcTypeInfo);
            case STRUCT         -> mapStruct(jdbcTypeInfo);
            case ARRAY          -> mapArray(jdbcTypeInfo);

            case REF            -> mapRef(jdbcTypeInfo);
            case DATALINK       -> mapDataLink(jdbcTypeInfo);
            case ROWID          -> mapRowId(jdbcTypeInfo);

            case SQLXML         -> mapSqlXML(jdbcTypeInfo);
            case REF_CURSOR     -> mapRefCursor(jdbcTypeInfo);
        };
    }


    protected abstract E mapBoolean(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapBit(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapTinyInt(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapInt(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapSmallInt(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapBigInt(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapFloat(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapDouble(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapReal(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapNumeric(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapDecimal(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapNVarChar(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapVarChar(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapChar(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapNChar(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapLongVarChar(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapLongNVarChar(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapNClob(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapClob(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapBinary(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapVarBinary(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapLongVarBinary(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapBlob(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapDate(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapTime(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapTimestamp(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapTimeWithTZ(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapTimestampWithTZ(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapNull(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapOther(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapJavaObject(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapDistinct(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapStruct(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapArray(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapRef(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapDataLink(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapRowId(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapSqlXML(JdbcTypeInfo jdbcTypeInfo);

    protected abstract E mapRefCursor(JdbcTypeInfo jdbcTypeInfo);

    protected void throwNotSupported(JdbcTypeInfo jdbcTypeInfo) {
        final String msg = String.format("JDBC type '%s/code:%d' not supported", jdbcTypeInfo.getJdbcType().getName(), jdbcTypeInfo.typeCode());
        throw new IllegalArgumentException(msg);
    }
}
