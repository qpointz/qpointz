# AI Regression Packs YAML Format Documentation

## Overview

AI regression packs use YAML files to define test scenarios that verify the behavior of the natural language to SQL (NL2SQL) system. Each scenario file contains a sequence of actions (queries and verifications) that validate different aspects of the system's functionality.

## File Structure

### Location
Scenario files are located in `ai/mill-ai-core/src/testIT/resources/scenarios/`

### YAML Structure

```yaml
name: <scenario-name>
parameters:
  reasoner: <reasoner-type>
  value-mapping: <optional-list-of-value-mappings>
run:
  - <action>
  - <action>
  ...
```

## Root Fields

### `name` (required)
String identifier for the scenario. Used in test reporting and logging.

### `parameters` (optional)
Configuration map for scenario execution:

- **`reasoner`** (optional, default: `"default"`): Specifies which reasoner to use
  - `"default"`: Standard reasoning without step-back clarification
  - `"step-back"`: Uses StepBackReasoner for clarification flow

- **`value-mapping`** (optional): List of value mapping documents for semantic value resolution
  - Each entry maps user-friendly terms to canonical database values
  - Used for fuzzy matching and language-specific value resolution

### `run` (required)
List of actions executed sequentially. Actions include `ask`, `verify`, and `reply`.

## Value Mapping Format

Value mappings enable semantic value resolution (e.g., translating "Korea" to "South Korea" in database queries):

```yaml
value-mapping:
  - id: <unique-id>
    target: [ <schema>, <table>, <column> ]
    value: <canonical-database-value>
    text: <display-text>
    context: <optional-context-hint>
    similarityThreshold: <optional-threshold-0.0-1.0>
```

**Example:**
```yaml
value-mapping:
  - { id: mcc-korea, target: [ MONETA, CLIENTS, COUNTRY ], value: "Korea", text: "Country name: Korea", context: "Country name", similarityThreshold: 0.4 }
```

## Actions

### `ask`
Sends a query to the chat application and waits for response.

**Syntax:**
```yaml
- ask: <query-text>
```

**Multiline queries:**
```yaml
- ask: |
    - add account and transactions information to result 
    - include only customers in ULTRA and WEALTH segment
```

**Behavior:**
- Executes the query through `ChatApplication.query()`
- Response stored in context for subsequent `verify` or `reply` actions
- Supports natural language queries in any supported language

### `reply`
Sends a clarification response in a step-back clarification flow.

**Syntax:**
```yaml
- reply:
    message: <message-index>
    with: <clarification-text>
```

**Parameters:**
- **`message`** (optional, default: `0`): Index of the previous message to reference (0 = most recent)
- **`with`** (required): The clarification text to send

**Behavior:**
- Extracts `reasoning-id` from the specified previous message
- Sends clarification using `ChatUserRequests.clarify()`
- Used in step-back reasoner flows to provide answers to clarification questions

**Example:**
```yaml
- reply:
    message: 1
    with: Premium clients are clients in ULTRA and WEALTH segment
```

### `verify`
Validates the response from a previous action using one or more checks.

**Syntax:**
```yaml
- verify:
    check:
      - <check-specification>
      - <check-specification>
      ...
```

**Optional parameters:**
- **`message`** (optional, default: `0`): Index of the message to verify (0 = most recent)

**Multiple check blocks:**
```yaml
- verify:
    - check:
        - intent: get-data
    - check:
        - has:
            - sql
```

## Checks

Checks validate specific aspects of the response. Multiple checks can be combined in a single `verify` action.

### `intent`
Verifies the detected intent matches the expected value.

**Syntax:**
```yaml
- intent: <intent-name>
```

**Valid intents:**
- `get-data`: Retrieve tabular data
- `get-chart`: Retrieve data for visualization
- `explain`: Describe data structures or results
- `refine`: Refine/modify previous query
- `do-conversation`: Casual conversation (greetings, thanks, etc.)
- `enrich-model`: Provide domain knowledge/metadata
- `unsupported`: Request cannot be processed

**Example:**
```yaml
- verify:
    check:
      - intent: get-data
```

### `has`
Verifies the response contains specific keys.

**Syntax:**
```yaml
- has:
    - <key1>
    - <key2>
    - ...
```

**Common keys:**
- `sql`: Generated SQL query
- `data`: Query result data
- `reasoning-id`: UUID for clarification flows (step-back reasoner)
- `questions`: List of clarification questions (step-back reasoner)
- `step-back`: Step-back analysis summary (step-back reasoner)
- `chart`: Chart specification (for get-chart intent)
- `description`: Explanation text (for explain intent)
- `enrichment`: Enrichment metadata (for enrich-model intent)

**Example:**
```yaml
- verify:
    check:
      - has:
          - sql
          - data
          - reasoning-id
          - questions
```

### `sql-shape`
Validates structural characteristics of generated SQL.

**Syntax:**
```yaml
- sql-shape:
    <property>: <expected-boolean>
    ...
```

**Available properties:**
- `has-aggregation`: Contains aggregation functions (COUNT, SUM, AVG, MIN, MAX)
- `has-grouping`: Contains GROUP BY clause
- `has-where`: Contains WHERE clause
- `has-ordering`: Contains ORDER BY clause
- `has-limit`: Contains LIMIT clause
- `has-subquery`: Contains subquery
- `has-join`: Contains JOIN operations

**Example:**
```yaml
- verify:
    check:
      - sql-shape:
          has-where: true
          has-join: false
          has-limit: true
```

### `returns`
Validates the number of records returned by the query.

**Syntax:**
```yaml
- returns: <expectation>
```

**Valid expectations:**
- `not-empty`: Result must contain at least one record
- `empty`: Result must contain zero records
- `<integer>`: Exact number of records (e.g., `10`)

**Example:**
```yaml
- verify:
    check:
      - returns: not-empty
      - returns: 10
```

### `enrichment`
Validates enrichment metadata structure.

**Syntax:**
```yaml
- enrichment:
    types: [ <type1>, <type2>, ... ]
```

**Valid types:**
- `concept`: Domain concept definition
- `rule`: Business rule definition
- `relation`: Relationship definition

**Example:**
```yaml
- verify:
    check:
      - enrichment:
          types: [ concept, rule, relation ]
```

## Complete Examples

### Basic Query Flow
```yaml
name: basic-regression
parameters:
  reasoner: default

run:
  - ask: list clients in Korea
  - verify:
      check:
        - intent: get-data
        - has:
            - sql
            - data
        - sql-shape:
            has-where: true
        - returns: not-empty
```

### Step-Back Clarification Flow
```yaml
name: step-back-regression
parameters:
  reasoner: step-back

run:
  - ask: list premium clients in emerging countries
  - verify:
      check:
        - has:
            - reasoning-id
            - questions
            - step-back
  - reply:
      message: 1
      with: Premium clients are clients in ULTRA and WEALTH segment
  - verify:
      check:
        - has:
            - reasoning-id
            - questions
            - step-back
  - reply:
      message: 1
      with: Emerging countries are Brazil, South Africa and China
  - verify:
      check:
        - intent: get-data
        - has:
            - sql
            - data
        - sql-shape:
            has-where: true
        - returns: not-empty
```

### Value Mapping Example
```yaml
name: value-mapping-test
parameters:
  reasoner: default
  value-mapping:
    - { id: mcc-korea, target: [ MONETA, CLIENTS, COUNTRY ], value: "Korea", text: "Country name: Korea", context: "Country name", similarityThreshold: 0.4 }
    - { id: mcc-sgp, target: [ MONETA, CLIENTS, COUNTRY ], value: "Singapore", text: "Country name: Singapore", context: "Country name", similarityThreshold: 0.4 }

run:
  - ask: gib mir Kunden aus der Schweiz
  - verify:
      check:
        - intent: get-data
        - has:
            - sql
            - data
        - sql-shape:
            has-where: true
        - returns: not-empty
```

## Implementation Notes

- Scenarios are executed by `ChatAppScenarioRunner` which extends `ScenarioRunner`
- Context is managed by `ChatAppScenarioContext` which initializes `ChatApplication` with specified reasoner
- Checks are registered in `CheckRegistry` and executed by `VerifyAction`
- Response data is stored in context and can be referenced by index (0 = most recent)
- All checks in a `verify` block must pass for the action to succeed
- Failed checks produce detailed error messages with expected vs. actual values

## Running Scenarios

Scenarios are executed as JUnit tests. Each scenario file should have a corresponding test class extending `ChatAppScenarioBase`:

```java
public static class MyRegressionPack extends ChatAppScenarioBase {
    @Override
    protected InputStream getScenarioStream(ClassLoader classLoader) {
        return classLoader.getResourceAsStream("scenarios/my-scenario.yml");
    }
}
```

Register the test class in `RegressionScenarios.java` to enable test execution.

