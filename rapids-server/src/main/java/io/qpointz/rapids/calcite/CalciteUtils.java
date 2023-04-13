package io.qpointz.rapids.calcite;

import io.qpointz.rapids.ServerUtils;
import io.qpointz.rapids.graphql.GraphQLHandler;
import io.qpointz.rapids.graphql.GraphQlCalciteHandler;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.sql.SQLException;

@Slf4j
public class CalciteUtils {
    @ApplicationScoped
    public static CalciteHandler calciteHandler(CalciteConfig config) throws ClassNotFoundException, SQLException {
        ServerUtils.logConfig(log, CalciteConfig.configurationPrefix);
        log.debug("Calcite create handler");
        if (config.mode()== CalciteConfig.CalciteMode.STANDARD) {
            log.info("Standard calcite to be used");
            final var stdConfig = config.standard();
            return new StandardCalciteHanlder(stdConfig.getProperties());
        } else {
            throw new RuntimeException(String.format("Calcite mode %s not suppoprted", config.mode()));
        }
    }

    @ApplicationScoped
    public static GraphQLHandler graphQLHandler(CalciteHandler calciteHandler)  {
        return new GraphQlCalciteHandler(calciteHandler);
    }

}
