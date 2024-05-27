package io.qpointz.delta.lineage.commands;

import org.apache.calcite.sql.SqlDialect;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name="dialect")
public class Dialects implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        Arrays.stream(SqlDialect.DatabaseProduct.values())
                .map(Enum::toString)
                .sorted()
                .forEach(System.out::println);
        return 0;
    }

}
