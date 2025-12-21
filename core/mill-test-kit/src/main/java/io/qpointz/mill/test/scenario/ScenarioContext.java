package io.qpointz.mill.test.scenario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base context class for scenario execution.
 * Stores action results and provides access to them.
 * 
 * @param <T> the concrete type of the context (for type-safe subclasses)
 * @param <R> the type of action result stored in this context
 */
public abstract class ScenarioContext<T extends ScenarioContext<T, R>, R extends ActionResult> {

    private final List<R> results;

    /**
     * Creates a new scenario context.
     */
    protected ScenarioContext() {
        this.results = new ArrayList<>();
    }

    /**
     * Adds an action result to the context.
     *
     * @param result the action result to add
     */
    public void addResult(R result) {
        if (result == null) {
            throw new IllegalArgumentException("ActionResult cannot be null");
        }
        this.results.add(result);
    }

    /**
     * Gets all action results in execution order.
     *
     * @return an unmodifiable list of action results
     */
    public List<R> getResults() {
        return Collections.unmodifiableList(this.results);
    }

    /**
     * Gets the number of results stored in this context.
     *
     * @return the number of results
     */
    @JsonIgnore
    public int getResultCount() {
        return this.results.size();
    }

    /**
     * Gets the last action result, if any.
     *
     * @return the last result, or null if no results exist
     */
    @JsonIgnore
    public R getLastResult() {
        return this.results.isEmpty() ? null : this.results.get(this.results.size() - 1);
    }

    @JsonIgnore
    public R getLastResult(int offset) {
        val idx = this.getResults().size()-offset-1;
        return this.getResults().get(idx);
    }

    /**
     * Serializes this context to JSON format.
     *
     * @return JSON string representation of the context
     * @throws IOException if serialization fails
     */
    public String toJson() throws IOException {
        val mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    /**
     * Serializes this context to YAML format.
     *
     * @return YAML string representation of the context
     * @throws IOException if serialization fails
     */
    public String toYaml() throws IOException {
        val mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    /**
     * Serializes this context to JSON and writes it to an OutputStream.
     *
     * @param outputStream the output stream to write to
     * @throws IOException if serialization or writing fails
     */
    public void serializeToJson(OutputStream outputStream) throws IOException {
        val mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, this);
    }

    /**
     * Serializes this context to YAML and writes it to an OutputStream.
     *
     * @param outputStream the output stream to write to
     * @throws IOException if serialization or writing fails
     */
    public void serializeToYaml(OutputStream outputStream) throws IOException {
        val mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, this);
    }
}

