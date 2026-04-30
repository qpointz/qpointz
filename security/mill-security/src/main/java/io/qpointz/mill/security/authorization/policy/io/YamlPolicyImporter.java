package io.qpointz.mill.security.authorization.policy.io;

import tools.jackson.databind.MapperFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class YamlPolicyImporter implements PolicyImporter {

    private final YAMLMapper mapper;

    public YamlPolicyImporter() {
        this.mapper = YAMLMapper.builder()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
    }

    public YamlPolicyImporter(YAMLMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collection<Policy> importPolicies(InputStream source) throws IOException {
        var policySet = mapper.readValue(source, PolicySet.class);
        return policySet.getPolicies();
    }
}
