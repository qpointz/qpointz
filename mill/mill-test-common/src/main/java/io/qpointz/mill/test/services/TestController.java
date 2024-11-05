package io.qpointz.mill.test.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test-security/")
@Slf4j
@AutoConfiguration
@EnableWebSecurity
public class TestController {

    public TestController() {
        log.info("TestController");
    }


    public record AuthInfo(boolean authenticated, String principalName, List<String> authorities) {
    }

    @GetMapping("auth-info")
    public AuthInfo authInfo() {
        val ctx = SecurityContextHolder.getContext();
        val auth = ctx.getAuthentication();
        if (auth == null) {
            return new AuthInfo(false, "ANONYMOUS", List.of());
        }

        val authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthInfo(true, auth.getName(), authorities);
    }

}
