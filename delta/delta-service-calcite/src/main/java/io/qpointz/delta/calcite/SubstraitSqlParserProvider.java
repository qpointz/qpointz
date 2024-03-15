package io.qpointz.delta.calcite;

import io.qpointz.delta.calcite.istmus.SqlToSubstrait;
import io.qpointz.delta.service.SqlParserProvider;
import io.substrait.type.NamedStruct;
import io.substrait.type.Type;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.List;

@AllArgsConstructor
public class SubstraitSqlParserProvider implements SqlParserProvider {

    private final RelDataTypeFactory relDataTypeFactory;
    private final CalciteConnectionConfig calciteConnectionConfig;
    private final SqlParser.Config parserConfig;
    private final SchemaPlus schema;


    @Override
    public boolean getAcceptsSql() {
        return true;
    }

    @Override
    public ParseResult parse(String sql) {
        val sqlcnv = new SqlToSubstrait(null, this.relDataTypeFactory, this.calciteConnectionConfig, this.parserConfig);
        val builder = ParseResult.builder()
                .originalSql(sql);
        try {
            val p = sqlcnv.execute(sql, "", this.schema);
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
