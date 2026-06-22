package io.qpointz.mill.app;

import io.qpointz.mill.analysis.queries.web.AnalysisDialectRestController;
import io.qpointz.mill.analysis.queries.web.SavedQueriesRestController;
import io.qpointz.mill.data.schema.api.SchemaExceptionHandler;
import io.qpointz.mill.data.schema.api.SchemaExplorerController;
import io.qpointz.mill.data.schema.api.SchemaExplorerService;
import io.qpointz.mill.data.schema.api.SchemaExplorerServiceDescriptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for the Mill HTTP and batch runtime.
 *
 * <p>Component scanning covers the {@code io.qpointz} namespace; JPA entity scanning uses the same
 * base so persistence modules can register entities without extra configuration.
 *
 * <p>Analysis REST controllers are excluded from the global scan and registered only via
 * {@link io.qpointz.mill.analysis.queries.web.config.AnalysisQueriesWebAutoConfiguration}
 * when {@code SavedQueryCatalog} and related beans are present.
 *
 * <p>Schema explorer REST beans are excluded likewise and registered via
 * {@link io.qpointz.mill.data.schema.api.config.SchemaExplorerWebAutoConfiguration}
 * when {@code SchemaFacetService} is available.
 *
 * <p>{@link AutoConfiguration} types are excluded from the global scan so they load only through
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} with
 * correct ordering and conditions.
 */
@SpringBootApplication
@ComponentScan(
        basePackages = "io.qpointz",
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {
                        SavedQueriesRestController.class,
                        AnalysisDialectRestController.class,
                        SchemaExplorerController.class,
                        SchemaExplorerService.class,
                        SchemaExceptionHandler.class,
                        SchemaExplorerServiceDescriptor.class
                    }),
            @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = AutoConfiguration.class)
        })
@EntityScan(basePackages = "io.qpointz")
@EnableScheduling
public class MillService {

    /**
     * Boots the application using default Spring Boot configuration discovery.
     *
     * @param args standard Spring Boot command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(new Class[] {MillService.class}, args);
    }

}
