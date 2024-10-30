package io.qpointz.mill.test.services;

import io.qpointz.mill.test.ConditionalOnTestKit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnTestKit
@RestController
@RequestMapping("/testservice")
public class TestService {

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

}
