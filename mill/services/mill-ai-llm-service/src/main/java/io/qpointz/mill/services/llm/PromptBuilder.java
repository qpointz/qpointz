package io.qpointz.mill.services.llm;

import lombok.val;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PromptBuilder {

    private final String prompt;

    public PromptBuilder(String prompt) {
        this.prompt = prompt;
    }

    public static PromptBuilder fromFile(File file) throws IOException {
        val prm = Files.readString(file.toPath());
        return new PromptBuilder(prm);
    }

    public String prompt() {
        return this.prompt;
    }



}
