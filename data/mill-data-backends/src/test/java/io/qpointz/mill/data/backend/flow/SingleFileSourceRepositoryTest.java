package io.qpointz.mill.data.backend.flow;

import io.qpointz.mill.source.descriptor.SourceDescriptor;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class SingleFileSourceRepositoryTest {

    private static final Path DESCRIPTOR = Path.of("./config/test/flow-source.yaml");

    @Test
    void shouldReadSingleDescriptor() {
        val repo = new SingleFileSourceRepository(DESCRIPTOR);
        val descriptors = StreamSupport
                .stream(repo.getSourceDefinitions().spliterator(), false)
                .toList();

        assertEquals(1, descriptors.size());
        assertEquals("flowtest", descriptors.get(0).getName());
    }

    @Test
    void shouldExposeDescriptorPath() {
        val repo = new SingleFileSourceRepository(DESCRIPTOR);
        assertEquals(DESCRIPTOR, repo.getDescriptorPath());
    }

    @Test
    void shouldThrowOnMissingFile() {
        val repo = new SingleFileSourceRepository(Path.of("/nonexistent/path.yaml"));
        assertThrows(IllegalStateException.class, repo::getSourceDefinitions);
    }
}
