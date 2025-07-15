package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.Assert.*;

@Slf4j
public class TestITProfileTest {

    @Test
    void readProfiles() {
        val profiles = TestITProfile.profiles();
        assertFalse(profiles.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("io.qpointz.mill.TestITProfile#profileArgs")
    void testParams(TestITProfile profile) {
        log.info("Profile {}", profile);
    }



}
