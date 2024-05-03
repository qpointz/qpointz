package io.qpointz.delta.lineage;

import lombok.val;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.tools.RelConversionException;
import org.apache.calcite.tools.ValidationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

class LineageShuttleTest {

    private SqlParse parseFromResource(String path) {
        val stream = getClass().getResourceAsStream(path);
        val reader = new InputStreamReader(stream);
        return new SqlParse(SqlDialect.DatabaseProduct.POSTGRESQL) {
            @Override
            protected Iterator<Reader> getReaders() {
                return List.<Reader>of(reader).iterator();
            }
        };
    }

    private RelNode asRel(String sql) throws IOException, SqlParseException, ValidationException, RelConversionException {
        val p = parseFromResource("/TestModel.sql");
        p.parse();

        val planner = p.getLineageRepository().getPlanner();
        val parsed = planner.parse(sql);
        val validated = planner.validate(parsed);
        return planner.rel(validated).rel;
    }

    private LineageShuttle parseToShuttle(String sql)  {
        RelNode rel = null;
        try {
            rel = asRel(sql);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        } catch (RelConversionException e) {
            throw new RuntimeException(e);
        }
        val shuttle = new LineageShuttle();
        rel.accept(shuttle);
        return shuttle;
    }

    @Test
    void simpleQuery()  {
        val shuttle = parseToShuttle("select * from Person");
    }

    @Test
    @Disabled
    void joinQuery() {
        val s = parseToShuttle("SELECT p.*, pi.* FROM Person p INNER JOIN PersonItem pi ON p.id = pi.person_id");
    }

    @Test
    @Disabled
    void filterQuery() {
        val s = parseToShuttle("SELECT p.first_name||p.last_name as full_name FROM Person p WHERE id>100 ");
    }



}