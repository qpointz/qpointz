package io.qpointz.mill.services;

import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Collection<? extends GrantedAuthority> grantedAuthorities() {
        val authCtx = SecurityContextHolder.getContext().getAuthentication();
        return authCtx != null
                ? authCtx.getAuthorities()
                : Set.of();
    }
}
