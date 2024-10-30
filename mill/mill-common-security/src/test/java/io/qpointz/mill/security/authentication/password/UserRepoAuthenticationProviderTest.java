package io.qpointz.mill.security.authentication.password;

import io.qpointz.mill.security.authentication.BaseTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test-trivial")
class UserRepoAuthenticationProviderTest extends BaseTest {

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public ResourceLoader resourceLoader;

    private UserRepoAuthenticationProvider createFromYamlFile(String resourcePath) throws IOException {
        var file = resourceLoader.getResource(resourcePath).getFile();
        val userRepo = UserRepo.fromYaml(file);
        val provider = new UserRepoAuthenticationProvider(userRepo, passwordEncoder);
        assertNotNull(provider);
        return provider;
    }


    private UserRepoAuthenticationProvider getProvider() throws IOException {
        return createFromYamlFile("classpath:userstore/passwd.yml");
    }

    @Test
    void returnsAuthenticationWhenUserExists() throws IOException {
        final var prov = getProvider();
        val auth = prov.authenticate(new UsernamePasswordAuthenticationToken("usr2", "TestPassword"));

        val authNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(Set.of("user","admin","reader"), authNames);
    }

    @Test
    void authenticateShouldIgnoreUserNameCase() throws IOException {
        final var prov = getProvider();
        val au = prov.authenticate(new UsernamePasswordAuthenticationToken("USr2", "TestPassword"));
        assertNotNull(au);
    }

    @Test
    void authenticatePasswordIsCaseSensetive() throws IOException {
        val prov = getProvider();
        prov.authenticate(new UsernamePasswordAuthenticationToken("usr1", "Password"));
    }



    @Test
    void returnsExtraDetailsIfPresent(@Autowired PasswordEncoder passwordEncoder) throws IOException {
        val prov = getProvider();
        val mayBeUsr = prov.authenticate("usrdtl", "TestPassword");
        assertTrue(mayBeUsr.isPresent());
        val usr = mayBeUsr.get();
        assertTrue(!usr.getDetails().isEmpty());
        val expect = Map.of("email", "usrdt@domain.test",
                "first_name","John",
                "last_name", "Dow",
                "phone", "+9999 343 343 34343");
        assertEquals(expect, usr.getDetails());
    }

    @Test
    void returnsEmptyAuthoritiesIfNoRolesProvided(@Autowired PasswordEncoder passwordEncoder) throws IOException {
        val prov = getProvider();
        val mayBeUsr = prov.authenticate("usr1", "password");
        val usr = mayBeUsr.get();
        assertEquals(List.of(), usr.getAuthorities());
    }

    @Test
    void returnsUniqueListOfAuthorities(@Autowired PasswordEncoder passwordEncoder) throws IOException {
        val prov = getProvider();
        val mayBeUsr = prov.authenticate("usrdup", "password");
        val usr = mayBeUsr.get();
        val act = usr.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        val expt = List.of("user", "reader");
        assertEquals(expt, act);
    }



}