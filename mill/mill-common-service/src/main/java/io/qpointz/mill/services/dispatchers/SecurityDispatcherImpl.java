package io.qpointz.mill.services.dispatchers;

import io.qpointz.mill.services.SecurityProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Slf4j
public class SecurityDispatcherImpl implements SecurityDispatcher {

    private final SecurityProvider provider;

    public SecurityDispatcherImpl(SecurityProvider provider) {
        this.provider = provider == null
                ? new NoneSecurityProvider()
                : provider;
    }

    public String principalName() {
                return this.provider.getPrincipalName();
    }

    @Override
    public Collection<? extends GrantedAuthority> grantedAuthorities() {
        return this.provider.grantedAuthorities();
    }

    private class NoneSecurityProvider implements SecurityProvider {
        @Override
        public String getPrincipalName() {
            log.warn("None Security Provider used");
            return "ANONYMOUS";
        }

        @Override
        public Collection<? extends GrantedAuthority> grantedAuthorities() {
            return List.of();
        }
    }


}
