package io.qpointz.mill.security.authentication.oauth2;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Slf4j
public class OAuth2ResourceServiceAuthenticationMethod implements AuthenticationMethod {

    @Getter
    private final OAuth2ResourceServerProperties.Jwt jwt;

    @Getter
    private final AuthenticationProvider authenticationProvider;

    @Getter
    private final int methodPriority;

    @Getter
    private final ClientRegistrationRepository clientRegistrationRepository;

    /**
     * Optional absolute URL for {@code oauth2Login().defaultSuccessUrl} (e.g. Vite dev origin).
     * May be {@code null} or blank to omit that configuration.
     */
    @Getter
    private final String oauth2LoginDefaultSuccessUrl;

    /**
     * When non-blank {@link #oauth2LoginDefaultSuccessUrl} is set, whether to call
     * {@code defaultSuccessUrl(url, true)} (ignore saved requests) instead of {@code false}.
     */
    @Getter
    private final boolean oauth2LoginAlwaysUseDefaultSuccessUrl;

    /**
     * @param jwt                             JWT settings for the resource server
     * @param authenticationProvider          JWT authentication provider
     * @param methodPriority                  relative priority among {@link AuthenticationMethod}s
     * @param clientRegistrationRepository    OAuth2 client registrations (may be {@code null})
     * @param oauth2LoginDefaultSuccessUrl          optional post-login redirect URL; blank omits wiring
     * @param oauth2LoginAlwaysUseDefaultSuccessUrl second argument to {@code defaultSuccessUrl}
     */
    public OAuth2ResourceServiceAuthenticationMethod(
            OAuth2ResourceServerProperties.Jwt jwt,
            AuthenticationProvider authenticationProvider,
            int methodPriority,
            ClientRegistrationRepository clientRegistrationRepository,
            String oauth2LoginDefaultSuccessUrl,
            boolean oauth2LoginAlwaysUseDefaultSuccessUrl) {
        this.jwt = jwt;
        this.authenticationProvider = authenticationProvider;
        this.methodPriority = methodPriority;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.oauth2LoginDefaultSuccessUrl = oauth2LoginDefaultSuccessUrl;
        this.oauth2LoginAlwaysUseDefaultSuccessUrl = oauth2LoginAlwaysUseDefaultSuccessUrl;
    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.OAUTH2;
    }

    /**
     * Registers {@code oauth2Login} and the browser entry page for unauthenticated users.
     *
     * <p>The login page must match the packaged Mill UI SPA route (default {@code /app/login},
     * consistent with {@code mill.ui.app-base-path} and Vite {@code base: '/app/'}). OAuth2
     * authorization URLs remain Spring defaults at servlet root ({@code /oauth2/authorization/…}),
     * not under the SPA prefix. The legacy static page {@code /id/login.html} is not used for
     * interactive login.
     *
     * @param http the Spring {@link HttpSecurity} builder for the auth route filter chain
     * @throws Exception if {@code oauth2Login} configuration fails
     */
    @Override
    public void applyLoginConfig(HttpSecurity http) throws Exception {

        if (clientRegistrationRepository == null) {
            log.warn("OAuth2 authentication method enabled , but no client registration provided. Skipping configuration");
            return;
        }
        http.oauth2Login(oauth -> {
            oauth.loginPage("/app/login").permitAll();
            oauth.failureHandler(oauth2LoginFailureHandler());
            if (oauth2LoginDefaultSuccessUrl != null && !oauth2LoginDefaultSuccessUrl.isBlank()) {
                val target = oauth2LoginDefaultSuccessUrl.trim();
                log.info(
                        "oauth2Login default success URL: {} (alwaysUse={})",
                        target,
                        oauth2LoginAlwaysUseDefaultSuccessUrl);
                oauth.defaultSuccessUrl(target, oauth2LoginAlwaysUseDefaultSuccessUrl);
            }
        });
    }

    /**
     * Logs the root cause of failed OIDC / OAuth2 code exchanges, then redirects to the SPA login
     * route (same behavior as Spring defaults for {@code loginPage("/app/login")}).
     *
     * @return a failure handler for {@code oauth2Login}
     */
    private AuthenticationFailureHandler oauth2LoginFailureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            log.warn("OAuth2 login failed: {}", exception.getMessage(), exception);
            if (exception instanceof OAuth2AuthenticationException oae) {
                val err = oae.getError();
                log.warn(
                        "OAuth2 provider error: errorCode={} description={} uri={}",
                        err.getErrorCode(),
                        err.getDescription(),
                        err.getUri());
            }
            String ctx = request.getContextPath();
            if (ctx == null) {
                ctx = "";
            }
            response.sendRedirect(ctx + "/app/login?error");
        };
    }

    @Override
    public void applySecurityConfig(HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(ht ->ht
                .jwt(jwc->
                        jwc.jwkSetUri(jwt.getJwkSetUri())));
    }
}
