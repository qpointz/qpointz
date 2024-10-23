package io.qpointz.mill.services.calcite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@AllArgsConstructor
public class ConnectionContextFactory implements CalciteContextFactory {

    @Getter
    private Properties connectionProperties;

    private class ConnectionContext extends CalciteConnectionContextBase {

        @Getter
        private final CalciteConnection calciteConnection;

        public ConnectionContext(Properties connectionProperties) throws SQLException, ClassNotFoundException {
            Class.forName("org.apache.calcite.jdbc.Driver");
            this.calciteConnection = DriverManager
                    .getConnection("jdbc:calcite:", connectionProperties)
                    .unwrap(CalciteConnection.class);
        }

        @Override
        public void close() throws Exception {
            this.calciteConnection.close();
        }
    }

    @Override
    public CalciteContext createContext() throws Exception {
        return new ConnectionContext(this.connectionProperties);
    }

}
