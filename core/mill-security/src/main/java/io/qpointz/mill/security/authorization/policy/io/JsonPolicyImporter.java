package io.qpointz.mill.security.authorization.policy.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class JsonPolicyImporter implements PolicyImporter {

    private final ObjectMapper mapper;

    public JsonPolicyImporter() {
        this.mapper = new ObjectMapper();
    }

    public JsonPolicyImporter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collection<Policy> importPolicies(InputStream source) throws IOException {
        var policySet = mapper.readValue(source, PolicySet.class);
        return policySet.getPolicies();
    }
}
