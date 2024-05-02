package io.qpointz.delta.calcite;

import io.qpointz.delta.calcite.istmus.SqlToSubstrait;
import io.qpointz.delta.service.SqlParserProvider;
import io.substrait.type.ImmutableType;
import io.substrait.type.NamedStruct;
import io.substrait.type.Type;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.List;

@AllArgsConstructor
public class SubstraitSqlParserProvider implements SqlParserProvider {

    //private final RelDataTypeFactory relDataTypeFactory;
    //private final CalciteConnectionConfig calciteConnectionConfig;
    private final SqlParser.Config parserConfig;
    private final CalciteConnection calciteConnection;


    @Override
    public boolean getAcceptsSql() {
        return true;
    }

    @Override
    public ParseResult parse(String sql) {
        val sqlcnv = new SqlToSubstrait(null,
                this.calciteConnection.getTypeFactory(),
                this.calciteConnection.config(),
                this.parserConfig);

        val builder = ParseResult.builder()
                .originalSql(sql);
        try {
            val calciteSchema = calciteConnection.getRootSchema().unwrap(CalciteSchema.class);
            val p = sqlcnv.execute(sql, calciteSchema);
            builder.success(true)
                    .plan(p);
        } catch (Exception e) {
            builder.success(false)
                    .message(e.getMessage())
                    .exception(e);
        }
        return builder.build();
    }

}
