package io.qpointz.rapids.navigator.services;

import lombok.val;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private final Path contentRoot = Paths.get("project").toAbsolutePath();

    public record FileContent(String key, String title, List<FileContent> children) {

    }

    public List<FileContent> rootContent() throws IOException {
        return listContent(this.contentRoot);
    }

    public List<FileContent> listContent(Path path) throws IOException {
        return Files
                .walk(path, 1, FileVisitOption.FOLLOW_LINKS)
                .filter(k-> !k.equals(path))
                .map(this::pathToContent)
                .collect(Collectors.toList());
    }

    private FileContent pathToContent(Path path) {
        try {
            val relUri = contentRoot.toUri().relativize(path.toUri()).toString();
            return new FileContent(relUri, path.getFileName().toString(), listContent(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
