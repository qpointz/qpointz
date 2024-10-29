package io.qpointz.mill.security.authentication.password;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qpointz.mill.security.authentication.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRepoTest extends BaseTest {

    @Autowired
    public ResourceLoader resourceLoader;

    private UserRepo createFromYamlFile(String resourcePath) throws IOException {
        var file = resourceLoader.getResource(resourcePath).getFile();
        return UserRepo.fromYaml(file);
    }

    @Test
    void throwsOnMalformedYaml() {
        assertThrows(JsonProcessingException.class, ()-> createFromYamlFile("/userstore/passwd_corrupt.yml"));
    }

}