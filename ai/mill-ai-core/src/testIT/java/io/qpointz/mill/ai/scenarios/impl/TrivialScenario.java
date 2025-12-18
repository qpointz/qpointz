package io.qpointz.mill.ai.scenarios.impl;

import io.qpointz.mill.ai.scenarios.ChatAppScenarioBase;

import java.io.InputStream;

public class TrivialScenario extends ChatAppScenarioBase {

    @Override
    protected InputStream getScenarioStream(ClassLoader classLoader) {
        return classLoader.getResourceAsStream("scenarios/trivial.yml");
    }
}
