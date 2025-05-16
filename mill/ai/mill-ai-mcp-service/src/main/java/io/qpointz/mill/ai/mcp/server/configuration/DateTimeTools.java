package io.qpointz.mill.ai.mcp.server.configuration;

import io.qpointz.mill.services.annotations.ConditionalOnService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@ConditionalOnService("data-bot")
public class DateTimeTools {

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

}
