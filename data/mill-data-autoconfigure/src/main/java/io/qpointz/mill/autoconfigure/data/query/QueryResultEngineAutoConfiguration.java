package io.qpointz.mill.autoconfigure.data.query;

import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.dispatchers.DataOperationDispatcher;
import io.qpointz.mill.data.query.engine.DefaultQueryResultExecutionService;
import io.qpointz.mill.data.query.engine.QueryResultEngineSettings;
import io.qpointz.mill.data.query.engine.QueryResultExecutionService;
import io.qpointz.mill.data.query.engine.marshal.ResultMarshallerRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Registers the Spring-free query-result engine ({@link QueryResultExecutionService}) and SPI
 * marshaller registry when a {@link DataOperationDispatcher} is available.
 *
 * <p>This auto-configuration is independent of {@code mill-data-query-service}; use it (via
 * {@code mill-data-autoconfigure}) for in-process query sessions without pulling MVC REST.
 */
@AutoConfiguration(after = BackendAutoConfiguration.class)
@EnableConfigurationProperties(MillDataQueryProperties.class)
@ConditionalOnBean(DataOperationDispatcher.class)
@ConditionalOnProperty(prefix = "mill.data.query", name = "enabled", havingValue = "true", matchIfMissing = true)
public class QueryResultEngineAutoConfiguration {

    /**
     * @return SPI-backed marshaller registry (built-ins from {@code mill-data-query} classpath).
     */
    @Bean
    @ConditionalOnMissingBean
    public ResultMarshallerRegistry resultMarshallerRegistry() {
        return ResultMarshallerRegistry.load(Thread.currentThread().getContextClassLoader());
    }

    /**
     * @param dispatcher data-plane dispatcher from {@code DefaultServiceConfiguration}
     * @param registry marshaller registry
     * @param props engine tuning from {@code mill.data.query.*}
     * @return default query-result session engine
     */
    @Bean
    @ConditionalOnMissingBean
    public QueryResultExecutionService queryResultExecutionService(
            DataOperationDispatcher dispatcher,
            ResultMarshallerRegistry registry,
            MillDataQueryProperties props) {
        QueryResultEngineSettings settings = new QueryResultEngineSettings(
                props.getMaxMaterializedRows(),
                props.getSessionExpireAfterAccess(),
                props.getDefaultFetchSize(),
                props.getMaxPageSize());
        return new DefaultQueryResultExecutionService(dispatcher, registry, settings);
    }
}
