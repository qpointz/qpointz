package io.qpointz.mill.data.backend;

import java.util.Collection;

public interface SecurityProvider {
    String getPrincipalName();
    Collection<String> authorities();
}
