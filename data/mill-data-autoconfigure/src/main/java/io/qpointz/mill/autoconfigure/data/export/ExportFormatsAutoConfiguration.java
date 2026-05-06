package io.qpointz.mill.autoconfigure.data.export;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Registers the SPI-backed {@link io.qpointz.mill.source.export.ExportFormatRegistry} bean.
 */
@AutoConfiguration
public class ExportFormatsAutoConfiguration {

    /**
     * Uses the application {@link ClassLoader} so {@link java.util.ServiceLoader} can see
     * {@code META-INF/services} entries from every format JAR on the runtime classpath. The loader
     * that loaded {@link AggregateExportFormatRegistry} alone can miss sibling modules in some
     * environments (IDE run configurations, layered class loaders).
     *
     * @param applicationContext Spring context (supplies the user-class class loader)
     * @return aggregate registry over {@link java.util.ServiceLoader} discoveries
     */
    @Bean
    public AggregateExportFormatRegistry exportFormatRegistry(ApplicationContext applicationContext) {
        ClassLoader loader = applicationContext.getClassLoader();
        return new AggregateExportFormatRegistry(loader);
    }
}
