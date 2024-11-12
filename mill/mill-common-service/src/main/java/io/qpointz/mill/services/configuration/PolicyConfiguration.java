package io.qpointz.mill.services.configuration;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "mill.security.authorization.policy")
@EnableConfigurationProperties
@Data
public class PolicyConfiguration {

    private Boolean enable;

    private Selector selector;

    public record GrantedAuthority(Map<String,String> remap)  {}


    public record Selector(GrantedAuthority grantedAuthority) {}

}
