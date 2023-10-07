package io.qpointz.rapids.calcite.schemas.blob.impl;

import io.qpointz.rapids.calcite.blob.*;
import java.util.Optional;
import java.util.regex.Pattern;

public class TestRegexBlobToTableMapper implements BlobToTableMapper {



    private final Pattern pattern;
    private final String datasetGroup;

    protected TestRegexBlobToTableMapper(String rx, String datasetGroup) {
        this.pattern = Pattern.compile(rx);
        this.datasetGroup = datasetGroup;
    }

    @Override
    public Optional<BlobToTableMappingInfo> mapPathToTable(BlobPath path) {
        final var m  = pattern.matcher(path.getPath());
        if (! m.matches()) {
            return Optional.empty();
        }

        var tableName = m.group(this.datasetGroup);

        /*var pm = new HashMap<String, PartitionValue>();

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
        */
        return Optional.of(BlobToTableMappingInfo.create(tableName));
    }
}
