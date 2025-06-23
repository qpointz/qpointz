package io.qpointz.mill.services.metadata.impl.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record FileRepository(
        @JsonProperty("model") Model model,
        @JsonProperty("schemas") Collection<Schema> schemas
) {
    public static FileRepository from(String location,
                                             ResourceLoader resourceLoader) throws IOException {
        return from(resourceLoader.getResource(location));
    }

    public static FileRepository from(Resource resource) throws IOException {
        return from(resource.getInputStream());
    }

    public static FileRepository from(InputStream inputStream) throws IOException {
        val mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        val result = mapper.readValue(inputStream, FileRepository.class);
        return result;
    }

    public record Model(
            @JsonProperty("name") Optional<String> name,
            @JsonProperty("description") Optional<String> description
    ) {}

    public record Schema (
            @JsonProperty("name") String name,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("tables") List<Table> tables,
            @JsonProperty("references") List<Relation> relations
    ) {}

    public record Relation(
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("parent") RelationParty parent,
            @JsonProperty("child") RelationParty child,
            @JsonProperty("cardinality") Optional<String> cardinality
    ) {}

    public record RelationParty(
            @JsonProperty("table") String table,
            @JsonProperty("attribute") String attribute
    ) {}

    public record Table(
            @JsonProperty("name") String name,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("attributes") List<Attribute> attributes
    ) {}

    public record Attribute(
            @JsonProperty("name") String name,
            @JsonProperty("description") Optional<String> description,
            @JsonProperty("type") Optional<String> typeName
    ) {}
}

