package io.qpointz.mill.data.backend.dispatchers;

import io.qpointz.mill.data.backend.NoneSecurityProvider;
import io.qpointz.mill.data.backend.SecurityProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

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
    public Collection<String> authorities() {
        return this.provider.authorities();
    }

}
