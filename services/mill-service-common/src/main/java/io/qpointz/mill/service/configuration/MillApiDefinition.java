package io.qpointz.mill.service.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@OpenAPIDefinition(
        info = @Info(title = "Mill API", version = "1.0.0")
)
public class MillApiDefinition {
}
