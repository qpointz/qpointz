package io.qpointz.delta.calcite.providers;

import io.qpointz.delta.calcite.istmus.SqlToSubstrait;
import io.qpointz.delta.service.SqlProvider;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

@AllArgsConstructor
public class CalciteSqlProvider implements SqlProvider {

    private final SqlParser.Config parserConfig;
    private final CalciteConnection calciteConnection;

    @Override
    public ParseResult parseSql(String sql) {
        val calciteSchema = calciteConnection.getRootSchema().unwrap(CalciteSchema.class);
        val planConverter = new SqlToSubstrait(null,
                this.calciteConnection.getTypeFactory(),
                this.calciteConnection.config(),
                this.parserConfig);
        try {
            val plan = planConverter.execute(sql, calciteSchema);
            return ParseResult.success(plan);
        } catch (SqlParseException e) {
            return ParseResult.fail(e);
        }
    }
}
