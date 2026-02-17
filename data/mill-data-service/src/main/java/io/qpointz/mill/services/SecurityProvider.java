package io.qpointz.mill.services;

import java.util.Collection;

public interface SecurityProvider {
    String getPrincipalName();
    Collection<String> authorities();
}
