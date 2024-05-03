package io.qpointz.delta.lineage.commands;

import io.qpointz.delta.lineage.SqlParse;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;

import java.io.File;
import java.util.concurrent.Callable;
import static picocli.CommandLine.*;

@Command(name="report", description = "Lineage report",  mixinStandardHelpOptions = true)
public class Report implements Callable<Integer> {

    @Option(names={"-d", "--dialect"}, required = true,  description = "SQL Dialect")
    SqlDialect.DatabaseProduct databaseProduct;

    @Option(names={"-i", "--input"}, required = true, description = "Input file")
    File[] files;

    @Override
    public Integer call() throws Exception {
        val parse = SqlParse.create(files, databaseProduct);
        parse.parse();
        System.out.println(parse.report().toString());
        return 0;
    }

}
