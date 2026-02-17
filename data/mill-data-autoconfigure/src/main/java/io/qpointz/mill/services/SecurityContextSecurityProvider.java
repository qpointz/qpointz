package io.qpointz.mill.services;

import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Set;

public class SecurityContextSecurityProvider implements SecurityProvider {

    private static String anonymousName = "ANONYMOUS";

    @Override
    public String getPrincipalName() {
        val authCtx = SecurityContextHolder.getContext();
        return authCtx!=null && authCtx.getAuthentication()!=null && authCtx.getAuthentication().getPrincipal()!=null
                ? authCtx.getAuthentication().getName()
                : anonymousName;
    }

    @Override
    public Collection<String> authorities() {
        val authCtx = SecurityContextHolder.getContext().getAuthentication();
        return authCtx != null
                ? authCtx.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList()
                : Set.of();
    }
}
