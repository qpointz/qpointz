package io.qpointz.mill.security.authorization.policy.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class YamlPolicyExporter implements PolicyExporter {

    private final ObjectMapper mapper;

    public YamlPolicyExporter() {
        this.mapper = new ObjectMapper(
                new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public YamlPolicyExporter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void export(Collection<Policy> policies, OutputStream target) throws IOException {
        var policySet = PolicySet.of(policies);
        mapper.writeValue(target, policySet);
    }
}
