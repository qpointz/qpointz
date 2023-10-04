package io.qpointz.rapids.parcels.filesystem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@AllArgsConstructor
@Builder
public class TableRegexPartitionMatcher implements TablePartitionInfoMatcher {

    @Getter
    private final String regexPattern;

    @Getter
    private final String datasetGroup;

    @Getter
    private final Map<String, PartitionValueType> partitionValueGroups;

    @Override
    public Optional<TablePartitionInfo> match(URI uri) {
        return this.match(uri.toString());
    }

    public Optional<TablePartitionInfo> match(String val) {
        Pattern pattern = Pattern.compile(this.regexPattern);
        final var m  = pattern.matcher(val);
        if (! m.matches()) {
            return Optional.empty();
        }

        var builder = TablePartitionInfo.builder();
        builder.tableName(m.group(this.getDatasetGroup()));

        var pm = new HashMap<String,PartitionValue>();
        for (final var e: this.partitionValueGroups.entrySet())
        {
            final var key = e.getKey();

            Object value = switch (e.getValue()) {
                case STRING -> m.group(key);
                case INT -> Integer.parseInt(m.group(key));
                default -> throw new IllegalArgumentException("Unknow partition type %s".formatted(e.getValue()));
            };

            pm.put(key, new PartitionValue(e.getValue(), key, value));
        }
        builder.partitionMap(pm);
        return Optional.of(builder.build());
    }
}
