package io.qpointz.mill.lineage;

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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
        p.report();

        val planner = p.getLineageRepository().getPlanner();
        val parsed = planner.parse(sql);
        val validated = planner.validate(parsed);
        return planner.rel(validated).rel;
    }

    private LineageShuttle.RelNodeLineage parseToShuttle(String sql)  {
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
        return LineageShuttle.extract(rel);
    }

    @Test
    void simpleQuery()  {
        val s = parseToShuttle("select * from Person");
        assertEquals(
            Set.of(
                    LineageItems.TableAttribute.of(List.of("PERSON", "ID")),
                    LineageItems.TableAttribute.of(List.of("PERSON", "FIRST_NAME")),
                    LineageItems.TableAttribute.of(List.of("PERSON", "LAST_NAME"))
            ),
            s.flatAttributes());
    }

    @Test
    void joinQuery() {
        val s = parseToShuttle("SELECT p.*, pi.id FROM Person p INNER JOIN PersonItem pi ON p.id = pi.person_id");
        val atts = LineageItems.TableAttribute.of(
                List.of("PERSON", "ID"),
                List.of("PERSON", "FIRST_NAME"),
                List.of("PERSON", "LAST_NAME"),
                List.of("PERSONITEM", "ID")
        );
        assertEquals(atts, s.flatAttributes());
    }

    @Test
    @Disabled
    void filterQuery() {
        val s = parseToShuttle("SELECT p.first_name||p.last_name as full_name FROM Person p WHERE id>100 ");
        val atts = LineageItems.TableAttribute.of(
                List.of("PERSON", "FIRST_NAME"),
                List.of("PERSON", "LAST_NAME")
        );

        val used = LineageItems.TableAttribute.of(
                List.of("PERSON", "ID")
        );


        assertEquals(atts, s.flatAttributes());
        assertEquals(used, s.flatUsed());
    }



}