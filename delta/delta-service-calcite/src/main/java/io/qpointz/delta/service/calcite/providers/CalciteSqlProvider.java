package io.qpointz.delta.service.calcite.providers;

import io.qpointz.delta.service.calcite.istmus.SqlToSubstrait;
import io.qpointz.delta.service.SqlProvider;
import io.qpointz.delta.service.utils.SubstraitUtils;
import lombok.*;
import org.apache.calcite.sql.parser.SqlParseException;

@AllArgsConstructor
public class CalciteSqlProvider implements SqlProvider {

    @Getter
    private final CalciteContext calciteCtx;

    @SneakyThrows
    @Override
    public ParseResult parseSql(String sql) {
        val planConverter = new SqlToSubstrait(null,
                this.getCalciteCtx().getTypeFactory(),
                this.getCalciteCtx().getCalciteConnection().config(),
                this.getCalciteCtx().getParserConfig());
        try {
            val plan = planConverter.execute(sql, this.getCalciteCtx().getCalciteRootSchema());
            val proto = SubstraitUtils.protoToPlan(plan);
            return ParseResult.success(proto);
        } catch (SqlParseException e) {
            return ParseResult.fail(e);
        }
    }
}
