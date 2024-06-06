package io.qpointz.delta.types;

import java.sql.SQLException;
import java.sql.Types;

public class JdbcToDatabaseType {

    private JdbcToDatabaseType() {
    }

    public static JdbcToDatabaseType DEFAULT = new JdbcToDatabaseType();

    public DatabaseType jdbc(int jdbcType, boolean nullable, int prec, int scale) {
        return switch (jdbcType) {
            case Types.BOOLEAN      -> DatabaseType.bool(nullable);
            case Types.BIT          -> DatabaseType.bool(nullable);

            case Types.TINYINT      -> DatabaseType.i16(nullable);

            case Types.INTEGER      -> DatabaseType.i32(nullable);
            case Types.SMALLINT     -> DatabaseType.i32(nullable);

            case Types.BIGINT       -> DatabaseType.i64(nullable);

            case Types.FLOAT        -> DatabaseType.fp32(nullable, prec, scale);

            case Types.DOUBLE       -> DatabaseType.fp64(nullable, prec, scale);
            case Types.REAL         -> DatabaseType.fp64(nullable, prec, scale);
            case Types.NUMERIC      -> DatabaseType.fp64(nullable, prec, scale);
            case Types.DECIMAL      -> DatabaseType.fp64(nullable, prec, scale);

            case Types.NVARCHAR     -> DatabaseType.string(nullable, prec);
            case Types.VARCHAR      -> DatabaseType.string(nullable, prec);
            case Types.CHAR         -> DatabaseType.string(nullable, prec);
            case Types.NCHAR        -> DatabaseType.string(nullable, prec);
            case Types.LONGVARCHAR  -> DatabaseType.string(nullable, prec);
            case Types.NCLOB        -> DatabaseType.string(nullable, prec);
            case Types.CLOB         -> DatabaseType.string(nullable, prec);

            case Types.BINARY       -> DatabaseType.binary(nullable, prec);
            case Types.VARBINARY    -> DatabaseType.binary(nullable, prec);
            case Types.LONGVARBINARY-> DatabaseType.binary(nullable, prec);
            case Types.BLOB         -> DatabaseType.binary(nullable, prec);

            case Types.DATE         -> DatabaseType.date(nullable);
            case Types.TIME         -> DatabaseType.time(nullable);
            case Types.TIMESTAMP    -> DatabaseType.timetz(nullable);
            case Types.TIME_WITH_TIMEZONE       -> DatabaseType.timetz(nullable);
            case Types.TIMESTAMP_WITH_TIMEZONE  -> DatabaseType.timetz(nullable);
            default -> throw new IllegalArgumentException(String.format("Not supported JDBC type:typeCode=%d", jdbcType));
        };
    }
}

//case Types.NULL            ->
//case Types.OTHER           ->
//case Types.JAVA_OBJECT         ->
//case Types.DISTINCT            ->
//case Types.STRUCT              ->
//case Types.ARRAY               ->
//case Types.REF                 ->
//case Types.DATALINK ->
//case Types.ROWID ->
//case Types.SQLXML ->
//case Types.REF_CURSOR ->
//case Types.ARRAY -> throw new SQLException("ARRAY NOT SUPPORTED");