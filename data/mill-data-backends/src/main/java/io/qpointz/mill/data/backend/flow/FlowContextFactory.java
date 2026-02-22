package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.data.backend.calcite.CalciteConnectionContextBase;
import io.qpointz.mill.data.backend.calcite.CalciteContext;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.source.calcite.SourceSchemaManager;
import lombok.Getter;
import org.apache.calcite.jdbc.CalciteConnection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * {@link CalciteContextFactory} that populates the Calcite root schema with
 * {@code FlowSchema} instances materialised from a {@link SourceDefinitionRepository}.
 */
public class FlowContextFactory implements CalciteContextFactory {

    @Getter
    private final SourceDefinitionRepository repository;

    @Getter
    private final Properties connectionProperties;

    public FlowContextFactory(SourceDefinitionRepository repository, Properties connectionProperties) {
        this.repository = repository;
        this.connectionProperties = connectionProperties;
    }

    @Override
    public CalciteContext createContext() throws Exception {
        return new FlowConnectionContext(repository, connectionProperties);
    }

    private static class FlowConnectionContext extends CalciteConnectionContextBase {

        @Getter
        private final CalciteConnection calciteConnection;
        private final SourceSchemaManager schemaManager;

        FlowConnectionContext(SourceDefinitionRepository repository, Properties props) throws Exception {
            Class.forName("org.apache.calcite.jdbc.Driver");
            this.calciteConnection = DriverManager
                    .getConnection("jdbc:calcite:", props)
                    .unwrap(CalciteConnection.class);

            this.schemaManager = new SourceSchemaManager();
            for (var descriptor : repository.getSourceDefinitions()) {
                schemaManager.add(descriptor);
            }
            schemaManager.registerAll(getRootSchema());
        }

        @Override
        public void close() throws Exception {
            try {
                schemaManager.close();
            } finally {
                calciteConnection.close();
            }
        }
    }
}
