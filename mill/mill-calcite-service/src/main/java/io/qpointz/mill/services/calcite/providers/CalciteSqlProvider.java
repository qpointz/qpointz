package io.qpointz.mill.services.calcite.providers;

import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.calcite.istmus.SqlToSubstrait;
import io.qpointz.mill.services.SqlProvider;
import io.qpointz.mill.services.utils.SubstraitUtils;
import lombok.*;
import org.apache.calcite.sql.parser.SqlParseException;

@AllArgsConstructor
public class CalciteSqlProvider implements SqlProvider {

    @Getter
    private final CalciteContextFactory ctxFactory;

    @Override
    public ParseResult parseSql(String sql) {
        try (val ctx = this.ctxFactory.createContext()) {
            val planConverter = new SqlToSubstrait(null,
                    ctx.getTypeFactory(),
                    ctx.getCalciteConnection().config(),
                    ctx.getParserConfig());

            val plan = planConverter.execute(sql, ctx.getCalciteRootSchema());
            val proto = SubstraitUtils.protoToPlan(plan);
            return ParseResult.success(proto);
        }
        catch (Exception e) {
            return ParseResult.fail(e);
        }
    }
}
