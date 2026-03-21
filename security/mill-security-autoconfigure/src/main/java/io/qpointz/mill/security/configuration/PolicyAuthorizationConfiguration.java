package io.qpointz.mill.security.configuration;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authorization.policy.PolicyActionDescriptor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration properties and auto-configuration for policy-based authorization.
 *
 * <p>Binds the {@code mill.security.authorization.policy.*} configuration prefix
 * and is only registered when {@code mill.security.enable=true}.
 *
 * <p>This class merges the formerly separate {@code PolicyConfiguration} and
 * {@code PolicyActionsConfiguration} classes into a single cohesive binding:
 * <ul>
 *   <li>{@code mill.security.authorization.policy.enable} — enables policy
 *       authorization.</li>
 *   <li>{@code mill.security.authorization.policy.selector} — defines how
 *       granted authorities are mapped to policy subjects.</li>
 *   <li>{@code mill.security.authorization.policy.actions} — inline list of
 *       {@link PolicyActionDescriptor} entries that declare allow/deny rules.</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mill.security.authorization.policy")
@ConditionalOnSecurity
public class PolicyAuthorizationConfiguration {

    /**
     * Whether policy-based authorization is enabled.
     *
     * <p>Set {@code mill.security.authorization.policy.enable=true} to activate
     * policy evaluation for incoming requests.
     */
    @Getter
    @Setter
    private boolean enable;

    /**
     * Selector configuration that controls how a Spring Security
     * {@code GrantedAuthority} is mapped to a policy subject.
     */
    @Getter
    @Setter
    private Selector selector;

    /**
     * Inline array of policy action descriptors that define the allow/deny rules
     * evaluated during authorization.
     *
     * <p>Corresponds to {@code mill.security.authorization.policy.actions[*].*}.
     */
    @Getter
    @Setter
    private PolicyActionDescriptor[] actions;

    /**
     * Selector configuration for policy subject resolution.
     *
     * @param grantedAuthority the granted-authority mapping configuration
     */
    public record Selector(GrantedAuthority grantedAuthority) {}

    /**
     * Mapping configuration that remaps Spring Security granted-authority strings
     * to policy subject names.
     *
     * @param remap a map of source authority strings to target policy subject names
     */
    public record GrantedAuthority(Map<String, String> remap) {}

}
