package io.qpointz.mill.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves packaged UI assets from {@code classpath:/static/app/{version}/} at {@link MillUiProperties#getAppBasePath()}.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mill.ui.enabled", havingValue = "true", matchIfMissing = true)
public class MillUiWebConfig implements WebMvcConfigurer {

    private final MillUiProperties millUi;

    /**
     * {@inheritDoc}
     *
     * @param registry Spring MVC resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String base = millUi.getAppBasePath();
        if (!base.startsWith("/")) {
            base = "/" + base;
        }
        String pattern = base.endsWith("/") ? base + "**" : base + "/**";
        String location = "classpath:/static/app/" + millUi.getVersion() + "/";
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}
