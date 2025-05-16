package io.qpointz.mill.services.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@Slf4j
class SqlAgentTest {

    private final String modelName = "gpt-4.1";
    private final String apiKey = "";

    @Test
    void trivia() throws IOException {
//        val pb = PromptBuilder.fromFile(new File("../test/datasets/moneta/moneta.prompt"));
//        val agent = new SqlAgent(apiKey, modelName, pb);
//        val reso = agent.query("How many clients in each country. Present as pie chart");
//        ObjectMapper om = new ObjectMapper();
//        val tree = om.readTree(reso);
//        log.info(reso);
    }

}