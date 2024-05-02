package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.server.CalciteTestUtils;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.tools.RelConversionException;
import org.apache.calcite.tools.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorBlockIteratorTest {

    @Test
    void trivial() throws ValidationException, SqlParseException, RelConversionException, SQLException {
        final var ctx = CalciteTestUtils.ctx();
        final var sql = "SELECT `city` AS `c1`, `id`,`state`,`city` FROM `airlines`.`cities`";
        final var schema = ctx.schema(sql);
        final var rs = ctx.execQuery(sql);
        final var iterator = new VectorBlockBatchedIterator(schema, rs, 73);
        var result = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL), false)
                .toList();
        assertNotNull(result);
        assertTrue(result.size()>0);
    }
}