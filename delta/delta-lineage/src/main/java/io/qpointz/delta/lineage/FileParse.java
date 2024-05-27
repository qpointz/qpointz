package io.qpointz.delta.lineage;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlDialect;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileParse extends SqlParse {

    @Getter
    private final List<File> files;

    public FileParse(List<File> files, SqlDialect.DatabaseProduct product) {
        super(product);
        this.files = files;
    }

    public static SqlParse create(File[] files, SqlDialect.DatabaseProduct databaseProduct) throws IOException {
        val list = new ArrayList<File>();
        for (File file : files) {
            list.addAll(toFiles(file));
        }
        return new FileParse(list, databaseProduct);
    }

    private static List<File> toFiles(File file) throws IOException {
        if (file.isFile()) {
            return List.of(file);
        }

        return Files.walk(file.toPath(), FileVisitOption.FOLLOW_LINKS)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    @Override
    protected Iterator<Reader> getReaders() {
        return files.stream().map(k-> {
            try {
                log.info("Reading {}", k.getCanonicalFile().getAbsolutePath());
                return (Reader)new FileReader(k);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).iterator();
    }
}
