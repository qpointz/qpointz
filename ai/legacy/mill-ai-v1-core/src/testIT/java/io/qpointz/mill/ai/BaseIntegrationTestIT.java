package io.qpointz.mill.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@ComponentScan(basePackages = {"io.qpointz"})
@EnableAutoConfiguration
public abstract class BaseIntegrationTestIT {
}
