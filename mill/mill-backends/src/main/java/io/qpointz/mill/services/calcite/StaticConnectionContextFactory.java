package io.qpointz.mill.services.calcite;

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
            //no closable resources associated
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
