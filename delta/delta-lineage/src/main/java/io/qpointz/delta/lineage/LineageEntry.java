package io.qpointz.delta.lineage;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Paths;

//@SpringBootApplication
//@Slf4j
public class LineageEntry implements CommandLineRunner {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(LineageEntry.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        val product = SqlDialect.DatabaseProduct.POSTGRESQL;
        val parse = new SqlParse(Paths.get("./sql"), product);
        parse.parse();
    }
}
