package io.qpointz.mill.export;

import io.qpointz.mill.annotations.service.ConditionalOnService;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.backend.dispatchers.PlanDispatcher;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.data.backend.export.ExportVectorBlockSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Registers export orchestration beans when {@code mill.data.services.export} is enabled.
 */
@AutoConfiguration
@ConditionalOnService(value = "export", group = "data")
public class ExportServiceAutoConfiguration {

    /**
     * @param dispatcher SQL / plan execution
     * @param planDispatcher supplies {@link io.qpointz.mill.data.backend.dispatchers.PlanHelper}
     * @param substraitDispatcher Substrait proto round-trip
     * @return vector source for export HTTP layer
     */
    @Bean
    public ExportVectorBlockSource exportVectorBlockSource(
            DataOperationDispatcher dispatcher,
            PlanDispatcher planDispatcher,
            SubstraitDispatcher substraitDispatcher) {
        return new ExportVectorBlockSource(dispatcher, planDispatcher.plan(), substraitDispatcher);
    }
}
