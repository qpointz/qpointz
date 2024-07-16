package io.qpointz.mill.service.security;

import io.qpointz.mill.service.MillServiceBaseTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FileAuthenticationProviderTest extends MillServiceBaseTest {

    @Test
    void fromConfigThrowsWhenPathKeyMissing(@Autowired PasswordEncoder passwordEncoder) {
        val cfg = Map.<String,Object>of("type","file",
                                        "pathmisisng", "missing path");
        assertThrows(IllegalArgumentException.class, ()-> new FileBasedSecurityProviderFactory().createAuthenticationProvider(cfg, passwordEncoder));
    }

    @Test
    void createFromYamlFile(@Autowired PasswordEncoder passwordEncoder) throws FileNotFoundException {
        val filePath = FileAuthenticationProviderTest.class.getResource("/fileProviders/passwd.yml")
                .getFile()
                .toString();
        val cfg = Map.<String,Object>of("type", "file",
                                    "path", filePath);
        val prov = new FileBasedSecurityProviderFactory().createAuthenticationProvider(cfg,passwordEncoder);
        assertNotNull(prov);
    }


    private static FileBasedSecurityProviderFactory.FileBasedAuthenticationProvider getProvider(PasswordEncoder passwordEncoder) {
        val is = FileAuthenticationProviderTest.class.getResourceAsStream("/fileProviders/passwd.yml");
        val prov = FileBasedSecurityProviderFactory.fromYaml(is , passwordEncoder);
        return prov;
    }

    @Test
    void returnsAuthenticationWhenUserExists(@Autowired PasswordEncoder passwordEncoder) {
        final var prov = getProvider(passwordEncoder);
        val auth = prov.authenticate(new UsernamePasswordAuthenticationToken("usr2", "TestPassword"));

        val authNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(Set.of("user","admin","reader"), authNames);
    }

    @Test
    void authenticateShouldIgnoreUserNameCase(@Autowired PasswordEncoder passwordEncoder) {
        final var prov = getProvider(passwordEncoder);
        val au = prov.authenticate(new UsernamePasswordAuthenticationToken("USr2", "TestPassword"));
        assertNotNull(au);
    }

    @Test
    void authenticatePasswordIsCaseSensetive(@Autowired PasswordEncoder passwordEncoder) {
        val prov = getProvider(passwordEncoder);
        prov.authenticate(new UsernamePasswordAuthenticationToken("usr1", "Password"));
    }

    @Test
    void throwsOnMalformedYaml(@Autowired PasswordEncoder passwordEncoder) {
        val is = FileAuthenticationProviderTest.class.getResourceAsStream("/fileProviders/passwd_corrupt.yml");
        assertThrows(RuntimeException.class, ()-> FileBasedSecurityProviderFactory.fromYaml(is , passwordEncoder));
    }

    @Test
    void returnsExtraDetailsIfPresent(@Autowired PasswordEncoder passwordEncoder) {
        val prov = getProvider(passwordEncoder);
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
    void returnsEmptyAuthoritiesIfNoRolesProvided(@Autowired PasswordEncoder passwordEncoder) {
        val prov = getProvider(passwordEncoder);
        val mayBeUsr = prov.authenticate("usr1", "password");
        val usr = mayBeUsr.get();
        assertEquals(List.of(), usr.getAuthorities());
    }

    @Test
    void returnsUniqueListOfAuthorities(@Autowired PasswordEncoder passwordEncoder) {
        val prov = getProvider(passwordEncoder);
        val mayBeUsr = prov.authenticate("usrdup", "password");
        val usr = mayBeUsr.get();
        val act = usr.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        val expt = List.of("user", "reader");
        assertEquals(expt, act);
    }



}