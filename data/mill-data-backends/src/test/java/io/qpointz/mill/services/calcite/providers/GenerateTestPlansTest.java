package io.qpointz.mill.services.calcite.providers;

import com.google.protobuf.util.JsonFormat;
import io.qpointz.mill.services.calcite.BaseTest;
import io.qpointz.mill.services.calcite.CalciteContextFactory;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.CalciteSqlDialect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


class GenerateTestPlansTest extends BaseTest {

    @Autowired
    CalciteContextFactory ctxFactory;

    @Autowired
    SubstraitDispatcher substraitDispatcher;

    private static final SqlDialect dialect = CalciteSqlDialect.DEFAULT;

    private void sqlToPlan(Map<String, String> queries) throws IOException {
        val provider = new CalciteSqlProvider(ctxFactory, substraitDispatcher);
        val converter = new io.substrait.plan.PlanProtoConverter();

        for (val entry : queries.entrySet()) {
            val fileName = entry.getKey();
            val sql = entry.getValue();

            val pr = provider.parseSql(sql);
            val plan = pr.getPlan();
            val proto = converter.toProto(plan);

            val writer = new PrintWriter(new FileOutputStream("../../test/plans/" + fileName + ".json", false));
            JsonFormat.printer().appendTo(proto, writer);
            writer.flush();
            writer.close();
        }
    }

    @Test
    //@Disabled("Test used to generate test data. Should be run manually only.")
    void trivialConvert() throws IOException {
        sqlToPlan(Map.of(
                "trivial" , "SELECT * FROM `cmart`.`CLIENT`"
        ));
        /*val sql = new CalciteSqlProvider(ctxFactory);
        val pr = sql.parseSql("SELECT `id` as `NewId` FROM `cmart`.`client`");
        val plan = pr.getPlan();
        val pc = new CalcitePlanConverter(ctxFactory, dialect);
        val relNode = pc.toRelNode(plan);
        assertNotNull(relNode);*/
    }

}
