package io.qpointz.delta.calcite;

import io.qpointz.delta.calcite.configuration.CalciteDataServiceConfiguration;
import io.qpointz.delta.calcite.configuration.CalciteProvidersConfiguration;
import io.qpointz.delta.service.*;
/* import io.qpointz.delta.service.ui.UIConfig; */
import io.qpointz.delta.service.configuration.DeltaServiceConfiguration;
import lombok.val;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


public class CalciteDeltaService {
//    public CalciteDeltaService(@Autowired SchemaProvider schemaProvider,
//                               @Autowired ExecutionProvider executionProvider,
//                               @Autowired SqlParserProvider sqlParserProvider) {
//        super(schemaProvider, executionProvider, sqlParserProvider);
//    }
//
//    public final static Class<?>[] configClasses = {
//            CalciteDeltaService.class,
//            CalciteDataServiceConfiguration.class,
//            CalciteDeltaServiceCtx.class //,
//            //ServiceSecurityConfig.class
//    };

    public static void main(String[] args) {
        val configuration = DeltaServiceConfiguration
                .newConfiguration()
                .withProviders(CalciteProvidersConfiguration.class)
                .withAdditionalConfig(CalciteDataServiceConfiguration.class);
        DeltaService.run(configuration ,args);
    }


}