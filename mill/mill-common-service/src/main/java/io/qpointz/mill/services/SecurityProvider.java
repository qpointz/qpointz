package io.qpointz.mill.services;


import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

public interface SecurityProvider {
    String getPrincipalName();
    Collection<? extends GrantedAuthority> grantedAuthorities();
}
