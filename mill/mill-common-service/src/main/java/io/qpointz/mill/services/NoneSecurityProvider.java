package io.qpointz.mill.services;

public class NoneSecurityProvider implements SecurityProvider {
    @Override
    public String getPrincipalName() {
        return "ANONYMOUS";
    }
}
