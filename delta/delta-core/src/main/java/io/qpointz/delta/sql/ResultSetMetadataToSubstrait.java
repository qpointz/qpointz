package io.qpointz.delta.sql;

import io.qpointz.delta.proto.Field;
import io.qpointz.delta.proto.Schema;
import io.qpointz.delta.proto.Table;
import io.qpointz.delta.proto.VectorBlockSchema;
import io.qpointz.delta.sql.types.*;
import io.substrait.proto.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.val;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
public class ResultSetMetadataToSubstrait {

    private final ResultSetMetaData metaData;

    public Schema asSchema() throws SQLException {
        return Schema.newBuilder()
                .addAllTables(List.of(this.asTable()))
                .build();
    }

    public VectorBlockSchema asVectorBlockSchema() throws SQLException {
        return VectorBlockSchema.newBuilder()
                .addAllFields(this.asFields())
                .build();
    }

    public Table asTable() throws SQLException {
        return Table.newBuilder()
                .addAllFields(this.asFields())
                .build();
    }

    public List<Field> asFields() throws SQLException {
        val fields = new ArrayList<Field>();
        val colCnt = this.metaData.getColumnCount();
        for (var idx=1; idx<=colCnt; idx++) {
            fields.add(asField(idx));
        }
        return fields;
    }

    public List<TypeHandler> asTypeHandlers() throws SQLException {
        val fields = new ArrayList<TypeHandler>();
        val colCnt = this.metaData.getColumnCount();
        for (var idx=1; idx<=colCnt; idx++) {
            fields.add(asTypeHandler(idx));
        }
        return fields;
    }

    public Field asField(int idx) throws SQLException {
        val md = this.metaData;
        val fieldType = asTypeHandler(idx).toSubstrait();
        return Field.newBuilder()
                .setName(md.getColumnName(idx))
                .setIndex(idx)
                .setType(fieldType)
                .build();
    }

    private TypeHandler asTypeHandler(int idx) throws SQLException {
        val md = this.metaData;

        val nullable = md.isNullable(idx) == ResultSetMetaData.columnNullable
                ? Type.Nullability.NULLABILITY_NULLABLE
                : Type.Nullability.NULLABILITY_REQUIRED;

        val typeId = md.getColumnType(idx);
        val prec = md.getPrecision(idx);
        val scale = md.getScale(idx);

        TypeHandler typeHandler = switch (typeId) {
            case Types.BOOLEAN , Types.BIT -> BooleanTypeHandler.get(nullable);
            case Types.INTEGER  -> new IntegerTypeHandler(nullable);
            case Types.TINYINT -> new TinyIntTypeHandler(nullable);
            case Types.SMALLINT -> new SmallIntTypeHandler(nullable);
            case Types.NVARCHAR , Types.VARCHAR -> new VarCharTypeHandler(nullable, prec);
            case Types.CHAR , Types.NCHAR -> new CharTypeHandler(nullable, prec);

//            case Types.BIGINT          ->
//            case Types.FLOAT           ->
//            case Types.REAL            ->
//            case Types.DOUBLE          ->
//            case Types.NUMERIC         ->
//            case Types.DECIMAL         ->
//            case Types.CHAR            ->

//            case Types.LONGVARCHAR     ->
//            case Types.DATE            ->
//            case Types.TIME            ->
//            case Types.TIMESTAMP       ->
//            case Types.BINARY          ->
//            case Types.VARBINARY       ->
//            case Types.LONGVARBINARY   ->
//            case Types.NULL            ->
//            case Types.OTHER           ->
//            case Types.JAVA_OBJECT         ->
//            case Types.DISTINCT            ->
//            case Types.STRUCT              ->
//            case Types.ARRAY               ->
//            case Types.BLOB                ->
//            case Types.CLOB                ->
//            case Types.REF                 ->
//            case Types.DATALINK ->

//            case Types.ROWID ->

//            case Types.LONGNVARCHAR ->
//            case Types.NCLOB ->
//            case Types.SQLXML ->
//            case Types.REF_CURSOR ->
//            case Types.TIME_WITH_TIMEZONE ->
//            case Types.TIMESTAMP_WITH_TIMEZONE ->
            /*case Types.ARRAY -> throw new SQLException("ARRAY NOT SUPPORTED");
            case Types.BIGINT -> throw new SQLException("BIGINT NOT SUPPORTED");
            case Types.BIT -> throw new SQLException("BIT NOT SUPPORTED");
            case Types.BINARY ->*/
            default -> throw new SQLException("Unsupported type: " + md.getColumnTypeName(idx));
        };

        return typeHandler;
    }


}
