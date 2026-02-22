package io.qpointz.mill.data.backend.calcite.providers;

import com.google.protobuf.util.JsonFormat;
import io.qpointz.mill.data.backend.calcite.BaseTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


class GenerateTestPlansTest extends BaseTest {

    private void sqlToPlan(Map<String, String> queries) throws IOException {
        val runner = this.getContextRunner();
        val converter = new io.substrait.plan.PlanProtoConverter();

        for (val entry : queries.entrySet()) {
            val fileName = entry.getKey();
            val sql = entry.getValue();

            val pr = runner.getSqlProvider().parseSql(sql);
            val plan = pr.getPlan();
            val proto = converter.toProto(plan);

            val writer = new PrintWriter(new FileOutputStream("../../test/plans/" + fileName + ".json", false));
            JsonFormat.printer().appendTo(proto, writer);
            writer.flush();
            writer.close();
        }
    }

    @Test
    void trivialConvert() throws IOException {
        sqlToPlan(Map.of(
                "trivial", "SELECT * FROM `cmart`.`CLIENT`"
        ));
    }

}
