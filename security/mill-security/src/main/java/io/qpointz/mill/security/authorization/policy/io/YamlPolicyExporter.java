package io.qpointz.mill.security.authorization.policy.io;

import tools.jackson.databind.SerializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;
import tools.jackson.dataformat.yaml.YAMLWriteFeature;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class YamlPolicyExporter implements PolicyExporter {

    private final YAMLMapper mapper;

    public YamlPolicyExporter() {
        this.mapper = YAMLMapper.builder()
                .disable(YAMLWriteFeature.WRITE_DOC_START_MARKER)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    public YamlPolicyExporter(YAMLMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void export(Collection<Policy> policies, OutputStream target) throws IOException {
        var policySet = PolicySet.of(policies);
        mapper.writeValue(target, policySet);
    }
}
