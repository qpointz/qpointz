package io.qpointz.mill.security.authorization.policy.io;

import io.qpointz.mill.security.authorization.policy.model.Policy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public interface PolicyExporter {

    void export(Collection<Policy> policies, OutputStream target) throws IOException;
}
