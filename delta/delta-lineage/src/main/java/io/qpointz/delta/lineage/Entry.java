package io.qpointz.delta.lineage;

import io.qpointz.delta.lineage.commands.Root;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
public class Entry {

    public static void main(String[] args) {
        new CommandLine(Root.class).execute(args);
    }

}
