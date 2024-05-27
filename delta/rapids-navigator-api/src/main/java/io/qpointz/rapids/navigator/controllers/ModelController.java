package io.qpointz.rapids.navigator.controllers;

import io.qpointz.rapids.navigator.services.ModelService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ModelController {

    @Autowired
    private ModelService modelService;

    public record Model(String key, String data) {
    }

    @GetMapping("/api/model/{*modelPath}")
    public ResponseEntity<String> getModel(@PathVariable String modelPath) {
        val content = modelService.getByPathAsJsonString(modelPath);
        if (!content.exists()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(modelPath+" not found");
        }

        if (content.failed()) {
            return ResponseEntity.internalServerError()
                    .build();
        }

        return ResponseEntity.ok(content.content());
    }

    @PostMapping(value = "/api/model/{*modelPath}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Model> updateModel(@PathVariable String modelPath, @RequestBody Model model) {
        return ResponseEntity
                .ok(new Model(modelPath, model.data));
    }


}
