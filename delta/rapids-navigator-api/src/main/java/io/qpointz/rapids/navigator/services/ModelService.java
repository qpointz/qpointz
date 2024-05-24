package io.qpointz.rapids.navigator.services;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ModelService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ContentService contentService;

    private ContentService.Content getByPath(String modelPath) {
        String path = modelPath + ".json";
        log.info("Loading model {} from {}", modelPath, path);
        return contentService.getByPath(path);
    }

    public ContentService.Content getByPathAsJsonString(String modelPath) {
        return getByPath(modelPath);
    }
}
