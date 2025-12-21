package io.qpointz.mill.test.scenario;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Base class for running scenarios as JUnit parameterized tests.
 * Each action in the scenario becomes a separate test execution.
 * 
 * <p>Uses {@link TestInstance.Lifecycle#PER_CLASS} to allow instance methods
 * to be used with {@link MethodSource}, enabling access to scenario state.
 * 
 * <p>After all tests complete, the scenario context is automatically saved as JSON
 * to {@code build/reports/scenarios/<qualified class name><scenario-name>.json}.
 * The directory is created automatically if it doesn't exist.
 * 
 * <p>Subclasses should:
 * <ul>
 *   <li>Implement {@link #createRunner(ScenarioContext)} to provide a concrete runner</li>
 *   <li>Implement {@link #createContext(Scenario)} to provide a concrete context</li>
 *   <li>Implement {@link #getScenario()} to provide the scenario to test</li>
 *   <li>Optionally override {@link #getDisplayName(Action, int)} to customize test names</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * class MyScenarioTest extends ScenarioTestBase<MyContext, MyActionResult> {
 *     @Override
 *     protected ScenarioRunner<MyContext, MyActionResult> createRunner(MyContext context) {
 *         return new MyScenarioRunner(context);
 *     }
 *     
 *     @Override
 *     protected MyContext createContext(Scenario scenario) {
 *         return new MyContext();
 *     }
 *     
 *     @Override
 *     protected Scenario getScenario() {
 *         return Scenario.fromFile("my-scenario.yml");
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of scenario context
 * @param <R> the type of action result
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ScenarioTestBase<T extends ScenarioContext<T, R>, R extends ActionResult> {

    private Scenario scenario;
    private T context;
    private ScenarioRunner<T, R> runner;

    /**
     * Creates a scenario runner instance for executing actions.
     * 
     * @param context the scenario context to use
     * @return a scenario runner
     */
    protected abstract ScenarioRunner<T, R> createRunner(T context);

    /**
     * Creates a new scenario context instance.
     * The context is created once per test class instance and shared across all action executions.
     * The scenario is provided so the context can be initialized based on scenario parameters.
     * 
     * @param scenario the scenario being tested
     * @return a new scenario context
     */
    protected abstract T createContext(Scenario scenario);

    /**
     * Gets the scenario to test.
     * This method is called once per test class to load the scenario.
     * The scenario is cached after first access.
     * 
     * @return the scenario to test
     */
    protected abstract Scenario getScenario();

    /**
     * Gets the scenario, caching it after first access.
     * 
     * @return the cached scenario
     */
    private Scenario getCachedScenario() {
        if (scenario == null) {
            scenario = getScenario();
        }
        return scenario;
    }

    /**
     * Gets the scenario context, creating it once per test class instance.
     * The context is shared across all action executions in the class.
     * 
     * @return the scenario context
     */
    private T getContext() {
        if (context == null) {
            context = createContext(getCachedScenario());
        }
        return context;
    }

    /**
     * Gets the scenario runner, creating it once per test class instance.
     * The runner is shared across all action executions in the class.
     * 
     * @return the scenario runner
     */
    private ScenarioRunner<T, R> getRunner() {
        if (runner == null) {
            runner = createRunner(getContext());
        }
        return runner;
    }

    /**
     * Generates the display name for a test case.
     * Default implementation uses: "{scenarioName} - {actionNameOrFriendlyParams} [{index}]"
     * If the action has a name, it is used; otherwise, a friendly string representation
     * of the params is used.
     * 
     * @param action the action being tested
     * @param index the zero-based index of the action in the scenario
     * @return the display name for the test
     */
    protected String getDisplayName(Action action, int index) {
        String actionName = action.name()
                .orElseGet(() -> action.key() + friendlyParamsToString(action.params()));
        return String.format("%s - %s [%d]", getCachedScenario().name(), actionName, index);
    }

    /**
     * Converts action params to a friendly string representation.
     * Handles various param types (primitives, strings, objects, lists) in a readable way.
     * 
     * @param params the action parameters
     * @return a friendly string representation of the params, or empty string if params are empty
     */
    private String friendlyParamsToString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        // If there's a single "value" key, use it directly (common for string/number values)
        if (params.size() == 1 && params.containsKey("value")) {
            Object value = params.get("value");
            if (value instanceof String) {
                return "(" + value + ")";
            } else if (value instanceof Number || value instanceof Boolean) {
                return "(" + value + ")";
            }
        }

        // Build a readable representation
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Skip "items" key for lists - show count instead
            if ("items".equals(key) && value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> items = (List<Object>) value;
                sb.append(items.size()).append(" items");
            } else {
                sb.append(key).append("=").append(friendlyValueToString(value));
            }
        }
        sb.append(")");
        
        return sb.toString();
    }

    /**
     * Converts a single value to a friendly string representation.
     * 
     * @param value the value to convert
     * @return a friendly string representation
     */
    private String friendlyValueToString(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            String str = (String) value;
            // Truncate long strings
            if (str.length() > 30) {
                return str.substring(0, 27) + "...";
            }
            return str;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            return "[" + list.size() + " items]";
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return "{" + map.size() + " fields}";
        }
        // For other types, use toString but truncate if too long
        String str = value.toString();
        if (str.length() > 30) {
            return str.substring(0, 27) + "...";
        }
        return str;
    }

    /**
     * Provides actions for parameterized tests.
     * This method is called by JUnit to get the test parameters.
     * Each action in the scenario becomes one test case.
     * With PER_CLASS lifecycle, instance methods can be used.
     * 
     * @return stream of arguments: (action, displayName, index)
     */
    Stream<Arguments> provideActions() {
        Scenario scenario = getCachedScenario();
        List<Action> actions = scenario.run();
        
        return java.util.stream.IntStream.range(0, actions.size())
                .mapToObj(i -> {
                    Action action = actions.get(i);
                    String displayName = getDisplayName(action, i);
                    return Arguments.of(action, displayName, i);
                });
    }
    

    /**
     * Executes a single action from the scenario as a parameterized test.
     * Each action becomes a separate test execution in JUnit.
     * 
     * @param action the action to execute
     * @param displayName the display name for this test case
     * @param index the zero-based index of the action in the scenario
     */
    @ParameterizedTest(name = "{1}")
    @MethodSource("provideActions")
    @DisplayName("Execute action")
    void executeAction(Action action, String displayName, int index) {
        // Use shared context and runner for all actions in this test class
        T context = getContext();
        ScenarioRunner<T, R> runner = getRunner();
        
        // Execute only this specific action
        R result = runner.executeActionWithErrorHandling(action);
        context.addResult(result);
        
        // Assert that the action was successful
        // Subclasses can override this method to add custom assertions
        assertActionResult(result, action, index);
    }

    /**
     * Asserts the result of an action execution.
     * Default implementation checks that the action was successful.
     * Subclasses can override to add custom assertions.
     * 
     * @param result the action result
     * @param action the action that was executed
     * @param index the index of the action in the scenario
     */
    protected void assertActionResult(R result, Action action, int index) {
        org.junit.jupiter.api.Assertions.assertTrue(
                result.success(),
                String.format("Action '%s' at index %d failed: %s",
                        action.key(),
                        index,
                        result.errorMessage().orElse("Unknown error")));
    }

    /**
     * Saves the scenario context to a JSON file after all tests complete.
     * The file is saved to {@code build/reports/scenarios/<qualified class name><scenario-name>.json}.
     * If saving fails, a warning is printed but tests won't fail.
     */
    @AfterAll
    void saveContext() {
        try {
            T context = getContext();
            if (context == null) {
                return; // No context to save
            }
            
            Scenario scenario = getCachedScenario();
            String className = this.getClass().getName();
            String scenarioName = scenario.name();
            String fileName = className + scenarioName + ".json";
            
            Path reportsDir = Paths.get("build/reports/scenarios");
            Files.createDirectories(reportsDir);
            
            Path outputFile = reportsDir.resolve(fileName);

            if (outputFile.toFile().exists()) {
                outputFile.toFile().delete();
            }

            try (var outputStream = Files.newOutputStream(outputFile)) {
                context.serializeToJson(outputStream);
            }
        } catch (IOException e) {
            // Log warning but don't fail tests
            System.err.println("Failed to save scenario context: " + e.getMessage());
        }
    }
}

