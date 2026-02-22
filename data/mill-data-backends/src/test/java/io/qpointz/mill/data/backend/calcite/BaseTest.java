package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.sql.dialect.SqlDialectSpecs;
import io.qpointz.mill.test.data.backend.BackendContextRunner;
import io.qpointz.mill.test.data.backend.CalciteBackendContextRunner;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;

public abstract class BaseTest {

    @Getter(AccessLevel.PROTECTED)
    private final BackendContextRunner contextRunner = CalciteBackendContextRunner.calciteContext(
                "./config/test/model.yaml",
                    SqlDialectSpecs.CALCITE,
                    Map.of(),
                    null
            );
    
}
