package io.qpointz.mill.services;

import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextSecurityProvider implements SecurityProvider {

    private static String anonymousName = "ANONYMOUS";

    @Override
    public String getPrincipalName() {
        val authCtx = SecurityContextHolder.getContext();
        return authCtx!=null && authCtx.getAuthentication()!=null && authCtx.getAuthentication().getPrincipal()!=null
                ? authCtx.getAuthentication().getName()
                : anonymousName;
    }

}
