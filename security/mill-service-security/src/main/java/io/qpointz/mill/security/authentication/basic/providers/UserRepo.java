package io.qpointz.mill.security.authentication.basic.providers;

import tools.jackson.dataformat.yaml.YAMLMapper;
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
        val mapper = YAMLMapper.builder().findAndAddModules().build();
        return mapper.readValue(file, UserRepo.class);
    }

}
