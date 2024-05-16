package io.qpointz.rapids.navigator.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentService {

    @Autowired
    private ProjectService projectService;

    public record Content(String content, Boolean exists, MediaType mediaType, Throwable th) {

        public boolean failed() {
            return th!=null;
        }
    }

    public Content getByPath(String contentPath) {
        var file = Paths.get(projectService.getContentRoot().toString(), contentPath)
                .toFile();

        if (!file.exists()) {
            return new Content(null, false, null, null);
        }

        try {
            val content = Files.readString(file.toPath());
            return new Content(content, true, MediaType.TEXT_PLAIN, null);
        } catch (IOException e) {
            return new Content(null, false, null, e);
        }
    }

    public record FileContent(String key, String title, String model, List<FileContent> children) {
    }

    public List<FileContent> rootContent() throws IOException {
        return listContent(this.projectService.getContentRoot());
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
            val relUri = this.projectService.getContentRootURI().relativize(path.toUri()).toString();
            String model = null;
            if (path.toFile().isFile()) {
                val didx = relUri.lastIndexOf(".");
                if (didx>-1){
                    model = relUri.substring(0, didx);
                }
            }
            return new FileContent(relUri, path.getFileName().toString(), model,  listContent(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
