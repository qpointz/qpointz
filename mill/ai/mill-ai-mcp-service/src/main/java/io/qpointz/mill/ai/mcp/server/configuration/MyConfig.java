package io.qpointz.mill.ai.mcp.server.configuration;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

@Configuration
@EnableWebMvc
@ConditionalOnService("data-bot")
class MyConfig {

    @Bean
    public ToolCallbackProvider weatherTools(DateTimeTools weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }
}
