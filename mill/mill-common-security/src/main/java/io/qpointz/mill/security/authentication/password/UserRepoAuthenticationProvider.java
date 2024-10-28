package io.qpointz.mill.security.authentication.password;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Slf4j
public final class UserRepoAuthenticationProvider implements AuthenticationProvider {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    public UserRepoAuthenticationProvider(UserRepo usersRepo, PasswordEncoder passwordEncoder) {
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

