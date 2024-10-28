package io.qpointz.mill.security.authentication.password;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class UserRepo {

    @Getter
    @Setter
    private List<User> users;

    public static UserRepo fromYaml(@NonNull File file) throws IOException {
        log.info("Loading user repo from {}", file.getAbsolutePath());
        try (val stream = new FileInputStream(file)) {
            return fromYaml(stream);
        }
    }

    public static UserRepo fromYaml(InputStream file) throws IOException {
        val mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        return mapper.readValue(file, UserRepo.class);
    }

}
