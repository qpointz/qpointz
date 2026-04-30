package io.qpointz.mill.security.authorization.policy.io;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class JsonPolicyImporter implements PolicyImporter {

    private final JsonMapper mapper;

    public JsonPolicyImporter() {
        this.mapper = JsonMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
    }

    public JsonPolicyImporter(JsonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collection<Policy> importPolicies(InputStream source) throws IOException {
        var policySet = mapper.readValue(source, PolicySet.class);
        return policySet.getPolicies();
    }
}
