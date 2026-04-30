package io.qpointz.mill.security.authorization.policy.io;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class JsonPolicyExporter implements PolicyExporter {

    private final JsonMapper mapper;

    public JsonPolicyExporter() {
        this.mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    public JsonPolicyExporter(JsonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void export(Collection<Policy> policies, OutputStream target) throws IOException {
        var policySet = PolicySet.of(policies);
        mapper.writeValue(target, policySet);
    }
}
