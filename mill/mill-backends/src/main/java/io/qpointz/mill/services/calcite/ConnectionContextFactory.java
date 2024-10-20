package io.qpointz.mill.services.calcite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;

import java.sql.DriverManager;
import java.util.Properties;

@AllArgsConstructor
public class ConnectionContextFactory implements CalciteContextFactory {

    @Getter
    private Properties connectionProperties;

    public static class ConnectionContext extends CalciteConnectionContextBase {

        private final CalciteConnection calciteConnection;

        public ConnectionContext(CalciteConnection connection) {
            this.calciteConnection = connection;
        }

        @Override
        public CalciteConnection getCalciteConnection() {
            return this.calciteConnection;
        }

        @Override
        public void close() throws Exception {
            this.getCalciteConnection().close();
        }
    }

    @Override
    public CalciteContext createContext() throws Exception {
        Class.forName("org.apache.calcite.jdbc.Driver");
        val calciteConnection = DriverManager
                .getConnection("jdbc:calcite:", this.connectionProperties)
                .unwrap(CalciteConnection.class);
        return new ConnectionContext(calciteConnection);
    }
}
