package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.Assert.*;

@Slf4j
public class TestITProfileTest {

    @Test
    // Verifies environment/default profile resolution returns at least one profile.
    void readProfiles() {
        val profiles = TestITProfile.profiles();
        assertFalse(profiles.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    // Verifies profile argument materialization is valid for parameterized tests.
    void testParams(TestITProfile profile) {
        log.info("Profile {}", profile);
    }



}
