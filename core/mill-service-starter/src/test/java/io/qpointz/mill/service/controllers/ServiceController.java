package io.qpointz.mill.service.controllers;

import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services/security-test")
public class ServiceController {

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("username")
    public ResponseEntity<String> username() {
        val auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.ok("ANONYMOUS");
        }

        return ResponseEntity.ok(auth.getName());
    }
}
