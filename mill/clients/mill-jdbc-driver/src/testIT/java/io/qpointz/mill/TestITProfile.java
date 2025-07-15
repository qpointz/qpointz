package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public record TestITProfile(String host, int port, Protocol protocol, Boolean tls, Authentication auth) {

    public enum Protocol {
        HTTP,
        GRPC
    }

    public enum Authentication {
        NO_AUTH,
        BASIC,
    }


    private static Stream<Arguments> profileArgs() {
        return profiles().stream()
                .map(Arguments::of);
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("Jet(%s,%s%s)  %s:%s",
                this.protocol,
                this.auth.name(),
                this.tls ? ",TLS" : "",
                this.host,
                this.port);
    }

    public static List<TestITProfile> profiles()  {
        InputStream inStream;
        val env = System.getenv("TEST_PROFILES");
        if (env != null && !env.isBlank()) {
            log.info("Using test profiles from environment variable");
            try {
                inStream = new FileInputStream(env);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("Read default profiles from resource");
            inStream = TestITProfile.class.getResourceAsStream("/.test-profiles");
        }


        val reader = new BufferedReader(new InputStreamReader(inStream));
        String line;
        val ret = new ArrayList<TestITProfile>();
        try {
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                val elems = line.split(",");

                val profile = new TestITProfile(
                        elems[0].trim(), //host,
                        Integer.parseInt(elems[1].trim()), //port
                        Protocol.valueOf(elems[2].trim().toUpperCase()),
                        elems[3].trim().equalsIgnoreCase("Y"),
                        elems[4].trim().equalsIgnoreCase("N")
                            ? Authentication.NO_AUTH
                            : Authentication.valueOf(elems[4].trim().toUpperCase())
                );

                ret.add(profile);

                log.info("Profile:{}", profile);
            }
        }
        catch (Exception e) {
                throw new RuntimeException();
        }
        return ret;
    }

}
