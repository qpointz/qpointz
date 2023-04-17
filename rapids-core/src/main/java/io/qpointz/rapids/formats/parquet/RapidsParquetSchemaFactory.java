package io.qpointz.rapids.formats.parquet;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class RapidsParquetSchemaFactory implements SchemaFactory {

    private static String DIR_KEY = "rootDir";
    private static String RX_DATASET_GROUP_KEY = "rx.datasetGroup";

    private static String RX_PATTERN_KEY = "rx.pattern";

    public RapidsParquetSchemaFactory() {

    }

    @Override
    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {

        if (!operand.containsKey(DIR_KEY)) {
            throw new IllegalArgumentException("Missing '%s' parameter".formatted(DIR_KEY));
        }

        if (!operand.containsKey(RX_DATASET_GROUP_KEY)) {
            throw new IllegalArgumentException("Missing '%s' parameter".formatted(RX_DATASET_GROUP_KEY));
        }

        if (!operand.containsKey(RX_PATTERN_KEY)) {
            throw new IllegalArgumentException("Missing '%s' parameter".formatted(RX_PATTERN_KEY));
        }

        var fs = FileSystems.getDefault();
        var rootPath = operand.get(DIR_KEY).toString();
        var pattern = operand.get(RX_PATTERN_KEY).toString();
        var datasetGroup = operand.get(RX_DATASET_GROUP_KEY).toString();

        return new RapidsParquetSchema(fs, fs.getPath(rootPath), pattern, datasetGroup);
    }
}
