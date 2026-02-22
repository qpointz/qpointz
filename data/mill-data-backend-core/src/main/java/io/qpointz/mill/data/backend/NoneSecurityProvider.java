package io.qpointz.mill.data.backend;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

@Slf4j
public class NoneSecurityProvider implements SecurityProvider {

    @Override
    public String getPrincipalName() {
        log.warn("None Security Provider used");
        return "ANONYMOUS";
    }

    @Override
    public Collection<String> authorities() {
        return List.of();
    }

}
