package io.qpointz.mill.security.authorization.policy.io;

import io.qpointz.mill.security.authorization.policy.model.Policy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface PolicyImporter {

    Collection<Policy> importPolicies(InputStream source) throws IOException;
}
