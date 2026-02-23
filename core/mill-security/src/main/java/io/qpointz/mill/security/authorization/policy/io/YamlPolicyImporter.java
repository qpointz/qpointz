package io.qpointz.mill.security.authorization.policy.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.qpointz.mill.security.authorization.policy.model.Policy;
import io.qpointz.mill.security.authorization.policy.model.PolicySet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class YamlPolicyImporter implements PolicyImporter {

    private final ObjectMapper mapper;

    public YamlPolicyImporter() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    public YamlPolicyImporter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Collection<Policy> importPolicies(InputStream source) throws IOException {
        var policySet = mapper.readValue(source, PolicySet.class);
        return policySet.getPolicies();
    }
}
