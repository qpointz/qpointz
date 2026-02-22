package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.source.descriptor.SourceDescriptor;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class MultiFileSourceRepositoryTest {

    private static final Path SOURCE_1 = Path.of("./config/test/flow-source.yaml");
    private static final Path SOURCE_2 = Path.of("./config/test/flow-source-2.yaml");

    @Test
    void shouldReadMultipleDescriptors() {
        val repo = new MultiFileSourceRepository(List.of(SOURCE_1, SOURCE_2));
        val descriptors = StreamSupport
                .stream(repo.getSourceDefinitions().spliterator(), false)
                .toList();

        assertEquals(2, descriptors.size());
        assertEquals("flowtest", descriptors.get(0).getName());
        assertEquals("flowtest2", descriptors.get(1).getName());
    }

    @Test
    void shouldReadFromVarargs() {
        val repo = new MultiFileSourceRepository(SOURCE_1, SOURCE_2);
        val descriptors = StreamSupport
                .stream(repo.getSourceDefinitions().spliterator(), false)
                .toList();

        assertEquals(2, descriptors.size());
    }

    @Test
    void shouldReturnEmptyForNoFiles() {
        val repo = new MultiFileSourceRepository(List.of());
        val descriptors = StreamSupport
                .stream(repo.getSourceDefinitions().spliterator(), false)
                .toList();

        assertTrue(descriptors.isEmpty());
    }

    @Test
    void shouldThrowOnDuplicateNames() {
        val repo = new MultiFileSourceRepository(List.of(SOURCE_1, SOURCE_1));
        assertThrows(IllegalStateException.class, repo::getSourceDefinitions);
    }

    @Test
    void shouldThrowOnMissingFile() {
        val repo = new MultiFileSourceRepository(
                List.of(SOURCE_1, Path.of("/nonexistent/path.yaml")));
        assertThrows(IllegalStateException.class, repo::getSourceDefinitions);
    }

    @Test
    void shouldExposeDescriptorPaths() {
        val paths = List.of(SOURCE_1, SOURCE_2);
        val repo = new MultiFileSourceRepository(paths);
        assertEquals(paths, repo.getDescriptorPaths());
    }
}
