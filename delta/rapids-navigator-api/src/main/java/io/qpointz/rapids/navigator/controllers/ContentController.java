package io.qpointz.rapids.navigator.controllers;

import io.qpointz.rapids.navigator.services.ContentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    public ContentService contentService;

    @GetMapping("/api/content")
    public List<ContentService.FileContent> getContent() throws IOException {
        return contentService.rootContent();
    }

    @GetMapping("/api/content/{*contentPath}")
    public void getContent(@PathVariable String contentPath, HttpServletResponse response) throws IOException {
        val content = contentService.getByPath(contentPath);
        if (!content.exists()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ;
        }

        if (content.failed()) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ;
        }

        response.addHeader(HttpHeaders.CONTENT_TYPE, "");
        response.addHeader("X-Content-Type-Options", "nosniff");
        response.addHeader("X-Download-Options", "noopen");
        response.getOutputStream().print(content.content());
    }

}
