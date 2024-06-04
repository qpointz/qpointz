package io.qpointz.delta.service.calcite.configuration;

import io.qpointz.delta.service.calcite.providers.CalciteContext;
import io.substrait.extension.ExtensionCollector;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class CalciteServiceCalciteContextConfiguration {

    @Bean
    @Qualifier("CALCITE_CONNECTION_PROPS")
    public static Properties calciteConnectionProperties(CalciteConfiguration configuration) throws ClassNotFoundException, SQLException {
        val props = new Properties();
        props.putAll(configuration.getConnection());
        return props;
    }

    @Bean
    public static CalciteConnection calciteConnection(@Qualifier("CALCITE_CONNECTION_PROPS") Properties properties
    ) throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.calcite.jdbc.Driver");
        return DriverManager
                .getConnection("jdbc:calcite:", properties)
                .unwrap(CalciteConnection.class);
    }

    @Bean
    public static CalciteContext calciteContext(CalciteConnection calciteConnection) {
        return new CalciteContext(calciteConnection);
    }

    @Bean
    public static ExtensionCollector extensionCollector() {
        return new ExtensionCollector();
    }

}
