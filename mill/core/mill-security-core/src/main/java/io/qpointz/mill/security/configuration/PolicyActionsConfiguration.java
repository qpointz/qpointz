package io.qpointz.mill.security.configuration;

import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authorization.policy.PolicyActionDescriptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mill.security.authorization.policy")
@ConditionalOnSecurity
public class PolicyActionsConfiguration {

    @Getter
    @Setter
    public boolean enable;

    @Getter
    @Setter
    public PolicyActionDescriptor[] actions;

}
