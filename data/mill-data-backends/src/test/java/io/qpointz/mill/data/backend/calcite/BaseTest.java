package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.sql.v2.dialect.DialectRegistry;
import io.qpointz.mill.test.data.backend.BackendContextRunner;
import io.qpointz.mill.test.data.backend.CalciteBackendContextRunner;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;

public abstract class BaseTest {

    @Getter(AccessLevel.PROTECTED)
    private final BackendContextRunner contextRunner = CalciteBackendContextRunner.calciteContext(
                "./config/test/model.yaml",
                    DialectRegistry.fromClasspathDefaults().requireDialect("CALCITE"),
                    Map.of(),
                    null
            );
    
}
