package io.qpointz.rapids.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.calcite.runtime.Resources;

import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@AllArgsConstructor
public class PropertiesEntry {

    @Getter
    String key;

    @Getter
    String value;

    private static final Pattern kvpattern = Pattern.compile("^([^\\=]+)\\=(.*$)");
    public static PropertiesEntry fromString(String in) {
        var m = kvpattern.matcher(in);
        if (!m.matches()) {
            throw new IllegalArgumentException(String.format("Property value %s is wrong. Should be <key>=<value>", in));
        }

        return new PropertiesEntry(m.group(1), m.group(2));
    }

    public static Properties asProperties(Iterable<PropertiesEntry> iters) {
        var map = StreamSupport.stream(iters.spliterator(), false)
                    .collect(Collectors.toMap(k-> k.getKey(), v-> v.getValue()));
        var props = new Properties();
        props.putAll(map);
        return props;
    }


}
