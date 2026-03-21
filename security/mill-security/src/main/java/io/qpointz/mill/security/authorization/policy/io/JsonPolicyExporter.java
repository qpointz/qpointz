package io.qpointz.mill.security.authorization.policy.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public class JsonPolicyExporter implements PolicyExporter {

    private final ObjectMapper mapper;

    public JsonPolicyExporter() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public JsonPolicyExporter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void export(Collection<Policy> policies, OutputStream target) throws IOException {
        var policySet = PolicySet.of(policies);
        mapper.writeValue(target, policySet);
    }
}
