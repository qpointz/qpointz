package io.qpointz.mill.security;

import java.util.Collection;

public interface SecurityProvider {
    String getPrincipalName();
    Collection<String> authorities();
}
