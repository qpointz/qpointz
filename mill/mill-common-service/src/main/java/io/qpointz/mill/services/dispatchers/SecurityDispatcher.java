package io.qpointz.mill.services.dispatchers;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface SecurityDispatcher {

    String principalName();

    Collection<? extends GrantedAuthority> grantedAuthorities();

}
