package io.qpointz.mill.service.calcite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.calcite.jdbc.CalciteConnection;

@AllArgsConstructor
public class StaticConnectionContextFactory implements CalciteContextFactory {

    private CalciteConnection connection;

    @AllArgsConstructor
    public class StaticConnectionContext extends CalciteConnectionContextBase {

        @Getter
        private final CalciteConnection calciteConnection;

        @Override
        public void close() throws Exception {
        }

    }

    @Override
    public CalciteContext createContext() {
        return new StaticConnectionContext(this.connection);
    }

    public static StaticConnectionContextFactory create(CalciteConnection connection) {
        return new StaticConnectionContextFactory(connection);
    }
}
