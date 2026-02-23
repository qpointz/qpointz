package io.qpointz.mill.ui.grinder.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class GrinderUIWebConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    static final String DEFAULT_UI_VERSION = "v1";

    @Value("${mill.ui.version:" + DEFAULT_UI_VERSION +  "}")
    private String uiVersion;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "classpath:/static/app/" + uiVersion + "/";

        registry
                .addResourceHandler("/app/**")
                .addResourceLocations(location);
    }


}
