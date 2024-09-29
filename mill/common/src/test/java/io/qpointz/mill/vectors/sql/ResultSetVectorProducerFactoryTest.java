package io.qpointz.mill.vectors.sql;

import io.qpointz.mill.types.sql.DatabaseType;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetVectorProducerFactoryTest {

    @Test
    void trivialProducer() {
        val prod = ResultSetVectorProducerFactory.DEFAULT.fromDatabaseType(DatabaseType.bool(false));
        assertNotNull(prod);
    }

}