package io.qpointz.mill.services.metadata.impl.file;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValueMappingSampleTest {

    private FileAnnotationsRepository loadRepository() throws IOException {
        var resource = new ClassPathResource("metadata/value-mapping-sample.yml");
        try (var stream = resource.getInputStream()) {
            var fileRepository = FileRepository.from(stream);
            return new FileAnnotationsRepository(fileRepository);
        }
    }

    @Test
    void sampleFileShouldExpandStaticMappingsWithAliases() throws IOException {
        var repository = loadRepository();

        var mappings = repository.getAllValueMappings();
        assertEquals(8, mappings.size(), "Expected original mappings plus expanded aliases");

        var segmentMappings = mappings.stream()
                .filter(m -> m.attributeName().equals("SEGMENT"))
                .toList();
        assertEquals(5, segmentMappings.size(), "SEGMENT should include primary terms and aliases");

        var userTerms = segmentMappings.stream()
                .map(m -> m.mapping().userTerm())
                .toList();
        assertTrue(userTerms.containsAll(List.of("premium", "gold", "vip", "standard", "basic")));

        var premiumMapping = segmentMappings.stream()
                .filter(m -> m.mapping().userTerm().equals("premium"))
                .findFirst()
                .orElseThrow();
        assertEquals("SAMPLE.CUSTOMERS.SEGMENT", premiumMapping.getFullyQualifiedName());
        assertEquals("PREMIUM", premiumMapping.mapping().databaseValue());
        assertEquals("Premium Customers", premiumMapping.mapping().getDisplayValueOrDefault());

        var aliasMapping = segmentMappings.stream()
                .filter(m -> m.mapping().userTerm().equals("gold"))
                .findFirst()
                .orElseThrow();
        assertEquals("Alias of premium", aliasMapping.mapping().description().orElse(""));
    }

    @Test
    void sampleFileShouldExposeDynamicSources() throws IOException {
        var repository = loadRepository();

        var sources = repository.getAllValueMappingSources();
        assertEquals(2, sources.size(), "Expected sources for COUNTRY and STATUS attributes");

        var countrySource = sources.stream()
                .filter(source -> source.sourceName().equals("country_lookup"))
                .findFirst()
                .orElseThrow();
        assertEquals("SAMPLE.CUSTOMERS.COUNTRY", countrySource.getFullyQualifiedName());
        assertEquals(3600, countrySource.cacheTtlSeconds());
        assertTrue(countrySource.enabled());

        var statusSource = sources.stream()
                .filter(source -> source.sourceName().equals("status_lookup"))
                .findFirst()
                .orElseThrow();
        assertEquals("SAMPLE.CUSTOMERS.STATUS", statusSource.getFullyQualifiedName());
        assertEquals(900, statusSource.cacheTtlSeconds());
    }
}

