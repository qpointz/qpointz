package io.qpointz.mill.security;

import java.util.Collection;
import java.util.List;

public class NoneSecurityProvider implements SecurityProvider {

    @Override
    public String getPrincipalName() {
        return "ANONYMOUS";
    }

    @Override
    public Collection<String> authorities() {
        return List.of();
    }
}
