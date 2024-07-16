package io.qpointz.rapids.navigator.services;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ProjectService {

    @Getter
    private final Path contentRoot = Paths.get("project").toAbsolutePath();

    public URI getContentRootURI() {
        return this.contentRoot.toUri();
    }
}
