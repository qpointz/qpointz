package io.qpointz.mill.services.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class FileBasedSecurityProviderFactory implements SecurityProviderFactory<FileBasedSecurityProviderFactory.FileBasedAuthenticationProvider> {

    @Override
    public String getProviderKey() {
        return "file";
    }

    private static final String PATH_KEY = "path";

    @SneakyThrows
    @Override
    public FileBasedAuthenticationProvider createAuthenticationProvider(Map<String, Object> config, PasswordEncoder passwordEncoder) {
        if (!config.containsKey(PATH_KEY)) {
            throw new IllegalArgumentException("Missing required config key: " + PATH_KEY);
        }
        var path = config.get(PATH_KEY).toString();
        var file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException(new FileNotFoundException("File does not exist: " + path));
        }

        return fromYaml(new FileInputStream(file), passwordEncoder);
    }

    public static FileBasedAuthenticationProvider fromYaml(InputStream file, PasswordEncoder passwordEncoder)  {
        val mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            val users = mapper.readValue(file, UserRepo.class);
            val proviider = new FileBasedAuthenticationProvider(users, passwordEncoder);
            log.info("Created file authprovider from yaml");
            return proviider;
        } catch (IOException e) {
            log.error("Configuration read error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<AuthReaderType> getRequeiredAuthReaderTypes() {
        return Set.of(AuthReaderType.BASIC_GRPC);
    }

    @Slf4j
    public final static class FileBasedAuthenticationProvider implements AuthenticationProvider {

        private final UserRepo userRepo;

        private final PasswordEncoder passwordEncoder;

        public FileBasedAuthenticationProvider(UserRepo usersRepo, PasswordEncoder passwordEncoder) {
            this.userRepo = usersRepo;
            this.passwordEncoder = passwordEncoder;
        }

        private boolean matchesPassword(User u, String username, String password) {
            return u.getName().equalsIgnoreCase(username) && passwordEncoder.matches(password, u.getPassword());
        }

        public Optional<User> authenticate(String username, String password) {
            return this.userRepo.getUsers().stream()
                    .filter(k-> matchesPassword(k, username, password))
                    .findFirst();
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            val username = authentication.getName().toLowerCase();
            val password = authentication.getCredentials().toString();
            val found = authenticate(username, password);
            if (found.isEmpty()) {
                log.debug("User {} not found", username);
                return null;
            } else {
                val usr = found.get();
                log.debug("User {} authenticated", username);
                return new UsernamePasswordAuthenticationToken(
                        authentication.getPrincipal(),
                        authentication.getCredentials(),
                        usr.getAuthorities()
                );
            }
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class
                    .isAssignableFrom(authentication);
        }

    }

}
