package io.qpointz.delta.lineage.commands;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import static picocli.CommandLine.*;

import java.util.concurrent.Callable;

@Slf4j
@Command(name = "lineage", mixinStandardHelpOptions = true, description = "Lineage tool", version = "1.0",
        subcommands = {Dialects.class, Report.class}
)
public class Root implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }
}




