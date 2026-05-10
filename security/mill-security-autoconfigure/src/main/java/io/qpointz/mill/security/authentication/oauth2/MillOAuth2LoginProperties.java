package io.qpointz.mill.security.authentication.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional settings for Spring Security {@code oauth2Login} when Mill registers the client flow.
 */
@ConfigurationProperties(prefix = "mill.security.oauth2.login")
@Getter
@Setter
public class MillOAuth2LoginProperties {

    /**
     * Absolute URL the browser is sent to after a successful {@code oauth2Login} when no
     * {@link org.springframework.security.web.savedrequest.SavedRequest} takes precedence
     * (unless {@link #alwaysUseDefaultSuccessUrl} is {@code true}).
     * <p>
     * Use {@code http://localhost:5173/app/} when developing the SPA with Vite on port 5173 while
     * the OIDC callback remains on the Spring Boot port ({@code /login/oauth2/code/…}).
     * Leave unset in production so the default success handler (saved request or application root)
     * applies.
     */
    private String defaultSuccessUrl = "";

    /**
     * When {@code true} and {@link #defaultSuccessUrl} is non-blank, registers
     * {@code oauth2Login().defaultSuccessUrl(url, true)} so the configured URL always wins over
     * any {@link org.springframework.security.web.savedrequest.SavedRequest}. Recommended for
     * cross-origin SPA dev servers (e.g. Vite on another port) so incidental requests such as
     * favicons are never used as the post-OAuth redirect target.
     */
    private boolean alwaysUseDefaultSuccessUrl = false;
}
