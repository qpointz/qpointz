package io.qpointz.delta.calcite;

import io.qpointz.delta.service.SqlProvider;
import lombok.AllArgsConstructor;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.sql.parser.SqlParser;

@AllArgsConstructor
public class SubstraitSqlParserProvider implements SqlProvider {

    //private final RelDataTypeFactory relDataTypeFactory;
    //private final CalciteConnectionConfig calciteConnectionConfig;
    private final SqlParser.Config parserConfig;
    private final CalciteConnection calciteConnection;

    @Override
    public ParseResult parseSql(String sql) {
        return null;
    }


//    @Override
//    public boolean getAcceptsSql() {
//        return true;
//    }
//
//    @Override
//    public ParseResult parse(String sql) {
//        val sqlcnv = new SqlToSubstrait(null,
//                this.calciteConnection.getTypeFactory(),
//                this.calciteConnection.config(),
//                this.parserConfig);
//
//        val builder = ParseResult.builder()
//                .originalSql(sql);
//        try {
//            val calciteSchema = calciteConnection.getRootSchema().unwrap(CalciteSchema.class);
//            val p = sqlcnv.execute(sql, calciteSchema);
//            builder.success(true)
//                    .plan(p);
//        } catch (Exception e) {
//            builder.success(false)
//                    .message(e.getMessage())
//                    .exception(e);
//        }
//        return builder.build();
//    }

}
