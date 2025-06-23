package io.qpointz.mill.services.metadata.impl.file;

import io.qpointz.mill.services.metadata.AnnotationsRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Lazy
@Component
@ConditionalOnProperty(prefix = "mill.metadata", name = "annotations", havingValue = "file")
public class FileAnnotationsRepository implements AnnotationsRepository {

    private final FileRepository repository;

    public FileAnnotationsRepository(FileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> getModelName() {
        return repository.model().name();
    }

    @Override
    public Optional<String> getModelDescription() {
        return repository.model().description();
    }

    private Optional<FileRepository.Schema> schemaByName(String schemaName) {
        if (schemaName==null) {
            return Optional.empty();
        }
        return this.repository.schemas().stream()
                .filter(k -> k.name().compareToIgnoreCase(schemaName)==0)
                .findFirst();
    }

    @Override
    public Optional<String> getSchemaDescription(String schemaName) {
        return schemaByName(schemaName)
                .flatMap(k -> k.description());
    }

    @Override
    public Optional<String> getTableDescription(String schemaName, String tableName) {
        return tableByName(schemaName, tableName)
                .flatMap(FileRepository.Table::description);
    }

    private Optional<FileRepository.Table> tableByName(String schemaName, String tableName) {
        return schemaByName(schemaName).flatMap(k-> k.tables().stream()
                .filter(z-> z.name().compareToIgnoreCase(tableName)==0)
                .findFirst()
        );
    }

    @Override
    public Optional<String> getAttributeDescription(String schemaName, String tableName, String attributeNam) {
        return attributeByName(schemaName, tableName, attributeNam)
                .flatMap(FileRepository.Attribute::description);
    }

    private Optional<FileRepository.Attribute> attributeByName(String schemaName, String tableName, String attributeNam) {
        return tableByName(schemaName,tableName).flatMap(k-> k.attributes().stream()
                .filter(z-> z.name().compareToIgnoreCase(attributeNam)==0)
                .findFirst()
        );
    }


}
