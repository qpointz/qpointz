package io.qpointz.delta.service;

import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityProvider {

    private static String anonymousName = "ANONYMOUS";

    public String getPrincipalName() {
        val authCtx = SecurityContextHolder.getContext();
        return authCtx!=null && authCtx.getAuthentication()!=null
            ? authCtx.getAuthentication().getPrincipal().toString()
            : anonymousName;
    }

}
