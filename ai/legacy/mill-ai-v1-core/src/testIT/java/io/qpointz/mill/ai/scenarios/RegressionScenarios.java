package io.qpointz.mill.ai.scenarios;

import java.io.InputStream;


public class RegressionScenarios {

    public static class BasicRegressionPack extends ChatAppScenarioBase {
        @Override
        protected InputStream getScenarioStream(ClassLoader classLoader) {
            return classLoader.getResourceAsStream("scenarios/basic.yml");
        }
    }

    public static class StepBackRegressionPack extends ChatAppScenarioBase {
        @Override
        protected InputStream getScenarioStream(ClassLoader classLoader) {
            return classLoader.getResourceAsStream("scenarios/stepback.yml");
        }
    }
    
}