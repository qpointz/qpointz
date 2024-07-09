package io.qpointz.mill.lineage.commands;

import io.qpointz.mill.lineage.FileParse;
import lombok.val;
import org.apache.calcite.sql.SqlDialect;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name="parse", description = "Parses input",  mixinStandardHelpOptions = true)
public class Parse implements Callable<Integer> {

    @CommandLine.Option(names={"-d", "--dialect"}, required = true,  description = "SQL Dialect")
    SqlDialect.DatabaseProduct databaseProduct;

    @CommandLine.Option(names={"-i", "--input"}, required = true, description = "Input file")
    File[] files;

    @Override
    public Integer call() throws Exception {
        val parse = FileParse.create(files, databaseProduct);
        parse.parse();
        return 0;
    }
}
