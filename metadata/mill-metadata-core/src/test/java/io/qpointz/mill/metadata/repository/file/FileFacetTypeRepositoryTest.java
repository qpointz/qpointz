package io.qpointz.mill.metadata.repository.file;

import io.qpointz.mill.metadata.domain.FacetTypeDescriptor;
import io.qpointz.mill.metadata.domain.MetadataTargetType;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileFacetTypeRepositoryTest {

    private FileFacetTypeRepository createRepository() {
        return new FileFacetTypeRepository(
                List.of("classpath:metadata/facet-types-test.yml"),
                new ClasspathResourceResolver());
    }

    @Test
    void shouldLoadFacetTypes_fromYaml() {
        var repo = createRepository();
        Collection<FacetTypeDescriptor> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldFindByTypeKey() {
        var repo = createRepository();
        var descriptive = repo.findByTypeKey("descriptive");
        assertTrue(descriptive.isPresent());
        assertEquals("Descriptive", descriptive.get().getDisplayName());
        assertTrue(descriptive.get().isMandatory());
    }

    @Test
    void shouldLoadApplicableTo() {
        var repo = createRepository();
        var descriptive = repo.findByTypeKey("descriptive").orElseThrow();
        Set<MetadataTargetType> targets = descriptive.getApplicableTo();
        assertNotNull(targets);
        assertTrue(targets.contains(MetadataTargetType.TABLE));
        assertTrue(targets.contains(MetadataTargetType.SCHEMA));
        assertTrue(targets.contains(MetadataTargetType.ATTRIBUTE));
    }

    @Test
    void shouldLoadContentSchema() {
        var repo = createRepository();
        var audit = repo.findByTypeKey("custom-audit").orElseThrow();
        assertTrue(audit.hasContentSchema());
        assertNotNull(audit.getContentSchema());
        assertEquals("object", audit.getContentSchema().get("type"));
    }

    @Test
    void shouldSaveNewType() {
        var repo = createRepository();
        repo.save(FacetTypeDescriptor.builder()
                .typeKey("new-type")
                .displayName("New Type")
                .build());
        assertTrue(repo.existsByTypeKey("new-type"));
    }

    @Test
    void shouldDeleteType() {
        var repo = createRepository();
        assertTrue(repo.existsByTypeKey("descriptive"));
        repo.deleteByTypeKey("descriptive");
        assertFalse(repo.existsByTypeKey("descriptive"));
    }

    @Test
    void shouldReturnEmpty_forUnknownType() {
        var repo = createRepository();
        assertTrue(repo.findByTypeKey("nonexistent").isEmpty());
    }
}
