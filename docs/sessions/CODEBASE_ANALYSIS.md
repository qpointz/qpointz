# Mill Data Integration Platform - Codebase Analysis

**Generated:** November 5, 2025  
**Repository:** qpointz/mill

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Core Components](#core-components)
4. [Backend Providers](#backend-providers)
5. [Service Layer](#service-layer)
6. [AI/NL2SQL Layer](#ainl2sql-layer)
7. [Client Libraries](#client-libraries)
8. [Protocol & Data Model](#protocol--data-model)
9. [Configuration](#configuration)
10. [Security Model](#security-model)
11. [Testing Strategy](#testing-strategy)
12. [Build System](#build-system)
13. [Deployment](#deployment)
14. [Technology Stack](#technology-stack)
15. [Key Strengths & Areas for Enhancement](#key-strengths--areas-for-enhancement)

---

## Overview

**Mill** is an experimental data integration platform that provides:

- **Unified SQL querying** across multiple data formats (CSV, TSV, Parquet, Avro, JDBC databases)
- **RESTful and gRPC service layers** for seamless data access
- **Universal connectivity** through JDBC drivers and Python client libraries
- **AI-powered natural language interface** for data exploration using plain English
- **Fine-grained security** with role-based access control at both record and table levels

The platform is built as a microservices architecture on Spring Boot 3.x, emphasizing extensibility, security, and modern data access patterns.

---

## Architecture

### Multi-Module Gradle Structure

The repository is organized as a Gradle multi-project build with six main areas:

```
mill/
├── core/               # Foundation libraries (6 modules)
├── services/           # Runtime service implementations (3 modules)
├── ai/                 # AI/LLM integration and NL2SQL (2 modules)
├── clients/            # Client libraries (JDBC, Python, Spark)
├── apps/               # Demo applications
├── build-logic/        # Custom Gradle plugins
├── proto/              # Protocol Buffer definitions
├── flow/               # Data flow utilities (Excel, blob I/O)
├── docs/               # Documentation
└── infra/              # Infrastructure as code (Terraform)
```

### Architecture Patterns

- **Protocol-First Design**: Well-defined gRPC/protobuf contracts
- **Hexagonal Architecture**: Clear separation between domain, service, and infrastructure layers
- **Plugin-Based Extensibility**: Custom backends, authentication methods, SQL functions
- **Microservices**: Independent, deployable services (gRPC, HTTP, AI chat)

---

## Core Components

### 1. Core Libraries (`core/`)

#### `mill-core`

**Vector Data Representation**
- Custom columnar data format using Protocol Buffers
- Efficient for streaming large result sets
- Components:
  - `VectorProducer<T>`: Interface for creating columnar vectors
  - `VectorBlockIterator`: Streaming vector blocks
  - `VectorColumnReader`: Accessing columnar data
- Supported types: i32, i64, fp32, fp64, boolean, string, bytes
- Null representation with efficient bitmap
- Zero-copy serialization via protobuf

**SQL Utilities**
- `RecordReader`: Abstraction for reading result sets
- `RecordReaderResultSetBase`: JDBC ResultSet compatibility layer
- `VectorBlockRecordIterator`: Converting vectors to JDBC ResultSet
- JDBC metadata converters

**Type System**
- **Logical Types**: High-level types (DATE, TIMESTAMP, VARCHAR, DECIMAL)
- **Physical Types**: Storage representation (i32, i64, bytes)
- **Substrait Integration**: Bidirectional type conversion
- `LogicalType<T, P>`: Generic type with producer and converter

**Query Planning**
- `LogicalFunctionHelper`: Substrait plan manipulation utilities
- Integration with Substrait query plans (IR between SQL dialects)

#### `mill-security-core`

**Authentication Framework**
- Pluggable authentication methods via `AuthenticationMethod` interface
- Built-in providers:
  - **Basic Authentication**: File-based user store (YAML)
  - **OAuth2/JWT**: Bearer token validation
  - **Microsoft Entra ID**: Azure AD integration
- `AuthenticationMethods`: Registry of active authentication providers
- `CompositeAuthenticationReader`: Chain-of-responsibility pattern

**Authorization System**
- **Policy-Based Access Control** (PBAC)
- Components:
  - `PolicyRepository`: Storage for policy definitions
  - `PolicyEvaluator`: Evaluates ALLOW/DENY policies
  - `PolicySelector`: Context-aware policy selection
  - `ActionVerb`: ALLOW, DENY enums
- **Fine-Grained Security**:
  - Row-level: `ExpressionFilterAction` with SQL predicates
  - Column-level: Field projection control
  - Schema-level: Schema visibility rules
- Policy format:
  ```yaml
  policies:
    - name: "data_scientists"
      actions:
        - verb: ALLOW
          action:
            type: expression-filter
            subject: [schema, table]
            expression: "department = 'analytics'"
  ```

**Spring Security Integration**
- Separate security filter chains:
  - `/api/**`: Authenticated data access
  - `/app/**`: Authenticated web UI
  - `/auth/**`, `/id/**`, `/oauth2/**`: Public auth flows
- Conditional activation: `@ConditionalOnSecurity` annotation
- gRPC security with metadata-based authentication
- CSRF disabled for API, enabled for web UI

#### `mill-service-core`

**Service Orchestration**
- `ServiceHandler`: Facade for all operations
- `DataOperationDispatcher`: Routes queries to appropriate backend
- `MetadataProvider`: Schema discovery interface
- `SubstraitDispatcher`: Substrait plan processing

**Configuration Management**
- Spring Boot configuration properties
- `BackendConfiguration`: Backend-specific settings
- `@ConditionalOnProperty`: Feature toggling

#### `mill-starter-backends`

**Backend Abstraction**
- `ExecutionProvider`: Interface for query execution
- `SchemaProvider`: Interface for metadata discovery
- `SqlProvider`: SQL parsing and conversion
- `PlanConverter`: Substrait plan conversion

**Extension System**
- `ExtensionCollector`: Manages Substrait extensions
- Custom SQL function registration
- Function definitions in `extensions/functions.yml`

**Multi-Schema Support**
- Single schema mode (default)
- Multi-schema mode (enumerate all schemas)
- Catalog support for databases with catalog concept

#### `mill-starter-service`

**Common Service Startup**
- Web UI hosting (login pages, static assets)
- Common bean definitions
- Service descriptor generation

#### `mill-test-common`

**Test Utilities**
- Shared test fixtures
- H2 database test helpers
- Mock data generators

---

## Backend Providers

Mill supports two primary backend strategies, both leveraging Apache Calcite.

### Calcite Backend

**Configuration**:
```yaml
mill:
  backend:
    provider: calcite
    calcite:
      connection:
        model: ./config/model.yaml
        quoting: DOUBLE_QUOTE
        caseSensitive: true
```

**Features**:
- Uses Apache Calcite as query engine
- `CalciteContextFactory`: Creates Calcite JDBC connections
- `CalciteExecutionProvider`: 
  - Converts Substrait plans → Calcite RelNode
  - Executes via Calcite's optimizer and runtime
- `CalciteSchemaProvider`: Schema discovery from Calcite
- Supports multiple data sources via Calcite adapters:
  - CSV, JSON (via file adapter)
  - JDBC databases (via JDBC adapter)
  - Custom adapters

**Use Cases**:
- Querying flat files (CSV, JSON)
- Federating multiple sources in single query
- Custom data sources with Calcite adapters

### JDBC Backend

**Configuration**:
```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: jdbc:postgresql://localhost:5432/mydb
      driver: org.postgresql.Driver
      username: user
      password: pass
      target-schema: public
      multi-schema: false
```

**Features**:
- Direct JDBC connectivity to databases
- `JdbcExecutionProvider`:
  - Converts Substrait plans → dialect-specific SQL
  - Executes directly on target database
  - Leverages database's native optimizer
- `JdbcCalciteContextFactory`: Uses Calcite for SQL parsing/planning only
- `JdbcContextFactory`: Direct JDBC connection management
- Multi-schema enumeration for complex databases

**Supported Databases** (via included drivers):
- PostgreSQL
- MySQL / MariaDB
- H2, HSQLDB (embedded)
- SQL Server
- Oracle
- Trino
- DuckDB
- Snowflake
- ClickHouse
- SQLite

**Use Cases**:
- Direct database querying
- Leveraging database-specific optimizations
- Transactional workloads
- Large-scale data warehouses

### Dialect Support

Both backends support dialect-specific SQL generation:
- Identifier quoting (backticks, double quotes)
- Literal syntax (strings, dates, booleans)
- Function naming (SUBSTR vs SUBSTRING)
- LIMIT/OFFSET vs TOP/FETCH syntax

---

## Service Layer

### `mill-jet-grpc-service`

**gRPC Service Implementation**

Protocol: `proto/data_connect_svc.proto`

**Operations**:

1. **Handshake**: Capability negotiation
   ```protobuf
   rpc Handshake(HandshakeRequest) returns (HandshakeResponse);
   ```
   - Returns version, capabilities, authentication context

2. **Schema Discovery**:
   ```protobuf
   rpc ListSchemas(ListSchemasRequest) returns (ListSchemasResponse);
   rpc GetSchema(GetSchemaRequest) returns (GetSchemaResponse);
   ```
   - List available schemas
   - Get detailed schema (tables, columns, types)

3. **SQL Parsing**:
   ```protobuf
   rpc ParseSql(ParseSqlRequest) returns (ParseSqlResponse);
   ```
   - SQL → Substrait plan conversion
   - Validation without execution

4. **Query Execution**:
   ```protobuf
   rpc ExecQuery(QueryRequest) returns (stream QueryResultResponse);
   rpc SubmitQuery(QueryRequest) returns (QueryResultResponse);
   rpc FetchQueryResult(QueryResultRequest) returns (QueryResultResponse);
   ```
   - Streaming execution for large results
   - Paged execution with cursor support
   - Configurable fetch size

**Security**:
- `GrpcSecurityMetadataSource`: Per-method access control
- Metadata-based authentication (Basic, Bearer)
- Integration with Spring Security

**Configuration**:
```yaml
mill:
  services:
    grpc:
      enable: true
      port: 9099
      address: "*"  # or "localhost"
```

### `mill-jet-http-service`

**RESTful HTTP API**

- Wraps gRPC protocol in HTTP POST endpoints
- Content negotiation: `application/json`, `application/x-protobuf`
- Base path: `/api/service/v1/`

**Controller**: `AccessServiceController`

**Endpoints**:
- `POST /api/service/v1/Handshake`
- `POST /api/service/v1/GetSchema`
- `POST /api/service/v1/ListSchemas`
- `POST /api/service/v1/ParseSql`
- `POST /api/service/v1/ExecQuery`

**Request/Response Handling**:
- Automatic protobuf ↔ JSON conversion
- Accept/Content-Type header negotiation
- `MessageHelper`: Conversion utilities

**Configuration**:
```yaml
mill:
  services:
    jet-http:
      enable: true
```

### `mill-grinder-service` + UI

**Web-Based Data Exploration**

**Backend** (Java/Spring Boot):
- Serves React SPA
- API proxy to underlying services
- OpenAPI specification generation

**Frontend** (React + TypeScript):
- Location: `services/mill-grinder-ui/`
- Tech stack: React, Vite, TypeScript
- Components:
  - Chat interface (`component/chat/`)
  - Data visualization (`component/data/`)
  - Chart rendering (`ChartView.tsx`)
  - Layout (`Topbar`, `Footer`)

**Features**:
- Natural language chat interface
- Tabular data display
- Chart generation (bar, line, pie)
- Query history
- Intent-based UI (GetDataIntent, ExplainIntent, GetChartIntent)

**Build**:
```bash
cd services/mill-grinder-ui
npm install
npm run build
```

**Configuration**:
```yaml
mill:
  services:
    grinder:
      enable: true
```

---

## AI/NL2SQL Layer

The AI layer provides sophisticated natural language to SQL conversion using a two-phase approach.

### Architecture Overview

**Two-Phase Intent Resolution**:

1. **Reasoning Phase** (`ReasonCall`):
   - Analyzes user's natural language question
   - Determines intent type
   - Selects schema scope and strategy
   - Identifies required SQL features

2. **Execution Phase** (`IntentCall`):
   - Generates SQL query or explanation
   - Applies value mapping
   - Executes query
   - Returns formatted results

### Intent Types

Defined in `IntentSpecs.java`:

| Intent | Purpose | Output |
|--------|---------|--------|
| `get-data` | Retrieve tabular data | SQL query + results |
| `get-chart` | Generate visualization data | SQL + chart config |
| `explain` | Describe schema/query | Natural language explanation |
| `refine` | Modify previous query | Updated SQL |
| `enrich-model` | Add domain knowledge | Updated metadata |
| `unsupported` | Cannot process | Clarifying questions |

### Reasoning System

**Template**: `templates/nlsql/reason/system.prompt`

**Reasoning Output** (`ReasoningResponse`):
```json
{
  "intent": "get-data",
  "schemaScope": "partial",
  "schemaStrategy": "partial_runtime_injection",
  "selectedTables": ["customers", "orders"],
  "sqlFeatures": {
    "joins": true,
    "grouping": false,
    "ordering": true,
    "paging": false
  }
}
```

**Schema Strategies**:
- `full_in_system_prompt`: Embed entire schema in LLM prompt
- `partial_runtime_injection`: Only include relevant tables
- `none`: No schema needed (metadata queries)

**SQL Feature Detection**:
- `identifiers`: Quoting/casing rules
- `literals`: String/date/boolean literals
- `joins`: JOIN syntax
- `ordering`: ORDER BY, NULLS handling
- `grouping`: GROUP BY, HAVING
- `paging`: LIMIT/OFFSET/TOP

### SQL Generation

**Template**: `templates/nlsql/intent/get-data/user.prompt`

**Key Innovation: Value Mapping System**

Every constant value in SQL is replaced with a placeholder:

```sql
-- User asks: "Show premium customers in California"

-- Generated SQL:
SELECT * FROM customers 
WHERE segment = "@{SALES.CUSTOMERS.SEGMENT:segment_premium}"
  AND state = "@{SALES.CUSTOMERS.STATE:state_ca}"
```

**Value Mapping Metadata**:
```json
{
  "sql": "SELECT * FROM customers WHERE segment = @{...} AND state = @{...}",
  "query-name": "premium_customers_california",
  "explanation": "Retrieve premium customers located in California",
  "value-mapping": [
    {
      "placeholder": "segment_premium",
      "target": "SALES.CUSTOMERS.SEGMENT",
      "display": "premium",
      "resolved-value": "PREMIUM",
      "type": "string",
      "kind": "constant",
      "meaning": "Premium customer segment identifier"
    },
    {
      "placeholder": "state_ca",
      "target": "SALES.CUSTOMERS.STATE",
      "display": "California",
      "resolved-value": "CA",
      "type": "string",
      "kind": "constant",
      "meaning": "California state code"
    }
  ]
}
```

**Benefits**:
- Domain-specific value resolution (e.g., "California" → "CA")
- Audit trail of value mappings
- RAG-based value suggestion
- Multi-language support (user terms in any language)

### Value Resolution

**Components**:
- `ValueMapper`: Resolves user terms to database values
- `ValueMappingSources`: Configuration-driven term mappings
- `MapValueProcessor`: Post-processes placeholders in SQL

**Configuration** (`metadata/valueMapping.yaml`):
```yaml
mappings:
  - table: CUSTOMERS
    column: SEGMENT
    values:
      - user_term: "premium"
        db_value: "PREMIUM"
      - user_term: "gold"
        db_value: "PREMIUM"
  - table: CUSTOMERS
    column: STATE
    values:
      - user_term: "California"
        db_value: "CA"
      - user_term: "CA"
        db_value: "CA"
```

### Dialect Support

**Dialect Templates**: `templates/nlsql/dialects/`

Supported dialects:
- H2
- PostgreSQL
- MySQL
- Oracle
- SQL Server (MSSQL)
- Databricks
- DuckDB
- Trino
- Calcite (generic)
- DB2

**Dialect Configuration** (`dialects/postgres/postgres.yml`):
```yaml
identifier_quote: '"'
string_literal_quote: "'"
date_format: "YYYY-MM-DD"
timestamp_format: "YYYY-MM-DD HH24:MI:SS"
boolean_true: "TRUE"
boolean_false: "FALSE"
null_literal: "NULL"
limit_syntax: "LIMIT {n} OFFSET {m}"
```

### Chat Service

**`mill-ai-nlsql-chat-service`**

RESTful API for chat interactions.

**Endpoints** (`NlSqlChatController`):
- `POST /api/chat/chats` - Create new chat
- `GET /api/chat/chats` - List user's chats
- `GET /api/chat/chats/{id}` - Get chat details
- `PUT /api/chat/chats/{id}` - Update chat
- `DELETE /api/chat/chats/{id}` - Delete chat
- `POST /api/chat/chats/{id}/messages` - Send message
- `GET /api/chat/chats/{id}/messages` - Get messages

**Data Model**:
```java
public record Chat(
    String id,
    String userId,
    String title,
    Instant createdAt,
    Instant updatedAt
) {}

public record ChatMessage(
    String id,
    String chatId,
    String role,      // "user" or "assistant"
    String content,
    Instant timestamp
) {}
```

**Memory**:
- In-memory (development): `mill.ai.chat.memory=in-memory`
- JDBC (production): `mill.ai.chat.memory=jdbc`
- Conversation history maintained per chat

**LLM Integration**:
- Spring AI framework
- Supported providers:
  - OpenAI: `spring.ai.openai.api-key`
  - Azure OpenAI: `spring.ai.azure.openai.*`
  - Ollama (local): `spring.ai.ollama.base-url`
- Model selection: `mill.services.data-bot.model-name=gpt-4`

### Configuration

```yaml
mill:
  ai:
    nl2sql:
      enable: true
      dialect: POSTGRESQL
    chat:
      memory: jdbc
  services:
    data-bot:
      enable: true
      model-name: gpt-4

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4
```

---

## Client Libraries

### JDBC Driver (`mill-jdbc-driver`)

**Standard JDBC 4.x Driver**

**Connection String**:
```
jdbc:mill://hostname:port/schema?param=value
```

**Usage**:
```java
Class.forName("io.qpointz.mill.jdbc.MillDriver");
Connection conn = DriverManager.getConnection(
    "jdbc:mill://localhost:9099/public",
    "username",
    "password"
);
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM customers");
```

**Features**:
- Full JDBC metadata support
- Prepared statements
- Batch operations
- Streaming ResultSets
- Connection pooling compatible (HikariCP, etc.)

**Communication**:
- gRPC backend
- Protocol Buffer serialization
- Automatic reconnection

**Testing**:
- Integration tests in `src/testIT/java`
- Mock gRPC server tests

### Python Client (`mill-py`)

**Installation**:
```bash
pip install mill-py
```

**Dependencies**:
- `grpcio`: gRPC communication
- `protobuf`: Message serialization
- `pandas`: DataFrame integration
- `pyarrow`: Columnar data handling

**Usage**:
```python
from millclient.client import MillClient

client = MillClient(
    host="localhost",
    port=9099,
    username="user",
    password="pass"
)

# Execute query
result = client.execute("SELECT * FROM customers")

# Convert to pandas DataFrame
df = result.to_dataframe()

# Schema discovery
schemas = client.list_schemas()
schema = client.get_schema("public")
```

**Modules**:
- `client.py`: Main client interface
- `auth.py`: Authentication helpers (Basic, Bearer)
- `proto/`: Generated protobuf stubs
- `utils.py`: Conversion utilities

**Async Support**:
```python
import asyncio
from millclient.client import AsyncMillClient

async def query_data():
    async with AsyncMillClient(host="localhost", port=9099) as client:
        result = await client.execute_async("SELECT * FROM customers")
        return result.to_dataframe()
```

### Spark Connector (`mill-spark`)

**Scala-based Spark Data Source**

**Usage**:
```scala
import org.apache.spark.sql.SparkSession

val spark = SparkSession.builder()
  .appName("Mill Example")
  .master("local[*]")
  .getOrCreate()

val df = spark.read
  .format("io.qpointz.mill.spark")
  .option("host", "localhost")
  .option("port", "9099")
  .option("schema", "public")
  .option("table", "customers")
  .load()

df.show()
```

**Features**:
- Predicate pushdown
- Column pruning
- Partition pruning
- Spark SQL integration

**Build**: sbt-based (`build.sbt`)

---

## Protocol & Data Model

### Protocol Buffers

#### `data_connect_svc.proto`

**Service Definition**:
```protobuf
service DataConnectService {
  rpc Handshake (HandshakeRequest) returns (HandshakeResponse);
  rpc ListSchemas(ListSchemasRequest) returns (ListSchemasResponse);
  rpc GetSchema(GetSchemaRequest) returns (GetSchemaResponse);
  rpc ParseSql(ParseSqlRequest) returns(ParseSqlResponse);
  rpc ExecQuery(QueryRequest) returns(stream QueryResultResponse);
  rpc SubmitQuery(QueryRequest) returns (QueryResultResponse);
  rpc FetchQueryResult(QueryResultRequest) returns (QueryResultResponse);
}
```

**Key Messages**:
```protobuf
message HandshakeResponse {
  message Capabilities {
    bool supportSql = 1;
    bool supportResultPaging = 2;
  }
  
  ProtocolVersion version = 2;
  Capabilities capabilities = 3;
  AuthenticationContext authentication = 4;
}

message QueryRequest {
  QueryExecutionConfig config = 1;
  oneof query {
    substrait.Plan plan = 2;
    SQLStatement statement = 3;
  }
}

message QueryResultResponse {
  optional string pagingId = 1;
  optional VectorBlock vector = 2;
}
```

#### `vector.proto`

**Vector Block Structure**:
```protobuf
message VectorBlock {
  Schema schema = 1;
  repeated Vector vectors = 2;
  int32 vectorSize = 3;
}

message Vector {
  oneof vector {
    BoolVector boolVector = 1;
    I32Vector i32Vector = 2;
    I64Vector i64Vector = 3;
    Fp32Vector fp32Vector = 4;
    Fp64Vector fp64Vector = 5;
    StringVector stringVector = 6;
    ByteVector byteVector = 7;
  }
  NullMap nulls = 8;
}

message Schema {
  repeated Field fields = 1;
}

message Field {
  string name = 1;
  int32 fieldIdx = 2;
  DataType type = 3;
}
```

**Columnar Data Model**:
- Each `Vector` represents one column
- All vectors in a block have same length (`vectorSize`)
- Separate `NullMap` for efficient null handling
- Type-specific vector implementations (I32Vector, StringVector, etc.)

#### `statement.proto`

**SQL Statement**:
```protobuf
message SQLStatement {
  string sql = 1;
  repeated Parameter parameters = 2;
}

message Parameter {
  uint32 index = 1;
  optional string name = 2;
  DataType type = 3;
  oneof value {
    bool booleanValue = 10;
    string stringValue = 11;
    int32 int32Value = 12;
    int64 int64Value = 13;
    float floatValue = 14;
    double doubleValue = 15;
  }
}
```

**Parameterized Queries**:
- Positional parameters (index-based)
- Named parameters (optional)
- Type-safe parameter binding

### Substrait Integration

**Substrait as Query IR**

- Industry-standard query plan representation
- SQL-agnostic intermediate representation
- Enables cross-dialect translation

**Flow**:
```
SQL (Dialect A) → Substrait Plan → SQL (Dialect B)
                      ↓
              Execution Engine
```

**Components**:
- `substrait.plan.proto`: Plan message definitions
- `SubstraitDispatcher`: Plan processing
- `SubstraitRelNodeConverter`: Calcite integration
- `DataTypeToSubstrait`: Type conversion
- Extension functions: Custom function definitions

**Example Plan Elements**:
- Relations: Scan, Filter, Project, Join, Aggregate
- Expressions: Literals, Field references, Function calls
- Types: Comprehensive type system

---

## Configuration

### Backend Configuration

#### Calcite Backend

```yaml
mill:
  backend:
    provider: calcite
    calcite:
      connection:
        model: ./config/model.yaml      # Calcite model file
        quoting: DOUBLE_QUOTE            # or BACK_TICK
        caseSensitive: true
        unquotedCasing: UNCHANGED        # or UPPER, LOWER
        fun: oracle                      # Function naming style
        conformance: ORACLE_12           # SQL conformance level
```

**Calcite Model File** (`model.yaml`):
```yaml
version: '1.0'
schemas:
  - name: PUBLIC
    type: custom
    factory: org.apache.calcite.adapter.csv.CsvSchemaFactory
    operand:
      directory: ./data/csv
```

#### JDBC Backend

```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: jdbc:postgresql://localhost:5432/mydb
      driver: org.postgresql.Driver
      username: ${DB_USER}
      password: ${DB_PASS}
      target-schema: public              # Default schema
      schema: public                     # Schema filter
      catalog: mycatalog                 # Catalog filter
      multi-schema: false                # Enumerate all schemas
```

### Service Configuration

```yaml
mill:
  services:
    # gRPC Service
    grpc:
      enable: true
      port: 9099
      address: "*"                       # or "localhost"
    
    # HTTP Service
    jet-http:
      enable: true
    
    # Web UI
    grinder:
      enable: true
    
    # Metadata Service
    meta:
      enable: true
    
    # AI Chat Service
    ai-nl2data:
      enable: true
```

### Security Configuration

```yaml
mill:
  security:
    enable: true
    
    authentication:
      # Basic Authentication
      basic:
        enable: true
        file-store: "file:./passwd.yml"
      
      # OAuth2 / JWT
      oauth2-resource-server:
        enable: true
        jwk-set-uri: https://idp.example.com/.well-known/jwks.json
      
      # Microsoft Entra ID
      entra-id-token:
        enable: true
        tenant-id: ${AZURE_TENANT_ID}
        client-id: ${AZURE_CLIENT_ID}

    authorization:
      policies:
        - name: data_scientists
          roles: [DATA_SCIENTIST, ADMIN]
          actions:
            - verb: ALLOW
              action:
                type: expression-filter
                subject: [analytics, customer_data]
                expression: "region IN ('US', 'EU')"
```

**User Store** (`passwd.yml`):
```yaml
users:
  - username: alice
    password: $2a$10$...                # BCrypt hash
    roles: [ADMIN, DATA_SCIENTIST]
    enabled: true
  
  - username: bob
    password: $2a$10$...
    roles: [ANALYST]
    enabled: true
```

### AI Configuration

```yaml
mill:
  ai:
    nl2sql:
      enable: true
      dialect: POSTGRESQL                # SQL dialect for generation
    
    chat:
      memory: jdbc                       # or in-memory
  
  services:
    data-bot:
      enable: true
      model-name: gpt-4

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4
        temperature: 0.7
        max-tokens: 2000
    
    azure:
      openai:
        endpoint: ${AZURE_OPENAI_ENDPOINT}
        api-key: ${AZURE_OPENAI_KEY}
        deployment-name: gpt-4
    
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3
```

### Monitoring & Observability

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    io.qpointz.mill: DEBUG
    org.springframework.security: INFO
    io.grpc: WARN
```

---

## Security Model

### Authentication

**Multi-Provider Architecture**:

1. **Basic Authentication**:
   - Username/password from file store
   - BCrypt password hashing
   - HTTP Basic header: `Authorization: Basic base64(user:pass)`

2. **OAuth2/JWT**:
   - Bearer token validation
   - JWK set verification
   - HTTP header: `Authorization: Bearer <token>`

3. **Microsoft Entra ID**:
   - Azure AD token validation
   - Profile loading from Graph API
   - Role extraction from token claims

4. **Custom Authentication**:
   - Implement `AuthenticationMethod` interface
   - Register as Spring bean

**Authentication Flow**:
```
Request → AuthenticationReader → AuthenticationProvider → Authentication
```

**gRPC Authentication**:
```
Metadata: authorization=Basic abc123
       OR authorization=Bearer xyz789
```

### Authorization

**Policy-Based Access Control**:

**Policy Structure**:
```yaml
policies:
  - name: restricted_data_policy
    roles: [ANALYST, DATA_SCIENTIST]
    actions:
      # Row-level security
      - verb: ALLOW
        action:
          type: expression-filter
          subject: [sales, orders]
          expression: "created_date >= CURRENT_DATE - INTERVAL '90 days'"
      
      # Column-level security
      - verb: DENY
        action:
          type: field-projection
          subject: [sales, customers]
          fields: [ssn, credit_card]
      
      # Schema-level security
      - verb: ALLOW
        action:
          type: schema-access
          subject: [sales]
```

**Policy Evaluation**:
1. `PolicySelector` selects applicable policies based on user roles
2. `PolicyEvaluator` evaluates ALLOW/DENY rules
3. Actions applied to query plan:
   - Row filters added to WHERE clause
   - Columns removed from SELECT list
   - Schemas filtered from metadata

**Action Types**:
- `expression-filter`: SQL predicate for row filtering
- `field-projection`: Column inclusion/exclusion
- `schema-access`: Schema visibility

**Policy Precedence**:
- DENY takes precedence over ALLOW
- More specific policies override general ones

### Route Security

**HTTP Routes**:
- `/api/**`: Authenticated, stateless (JWT)
- `/app/**`: Authenticated, session-based
- `/auth/**`, `/id/**`, `/oauth2/**`: Public
- `/actuator/**`: Authenticated (optional)

**gRPC Security**:
- Per-service authentication requirement
- `GrpcSecurityMetadataSource` defines protected services
- `DataConnectService`: Requires authentication

---

## Testing Strategy

### Unit Tests

**Location**: `src/test/java`

**Framework**: JUnit 5 + Mockito

**Naming Conventions**:
- Test class: `<Subject>Test.java`
- Test method: `shouldX_whenY()` or `testX()`

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class PolicyEvaluatorTest {
    
    @Mock
    private PolicyRepository repository;
    
    @Mock
    private PolicySelector selector;
    
    @InjectMocks
    private PolicyEvaluatorImpl evaluator;
    
    @Test
    void shouldReturnFilterActions_whenAllowPolicySelected() {
        // Arrange
        when(selector.selectPolicies(any(), any()))
            .thenReturn(Set.of("policy1"));
        
        // Act
        var actions = evaluator.actionsBy(
            ExpressionFilterAction.class, 
            ALLOW, 
            List.of("schema", "table")
        );
        
        // Assert
        assertThat(actions).isNotEmpty();
    }
}
```

### Integration Tests

**Location**: `src/testIT/java`

**Purpose**: End-to-end testing with real components

**Example**:
```java
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "mill.backend.provider=jdbc",
        "mill.backend.jdbc.url=jdbc:h2:mem:test"
    }
)
class MillGrpcServiceExecuteTest {
    
    @Autowired
    private DataConnectServiceGrpc.DataConnectServiceBlockingStub stub;
    
    @Test
    void shouldExecuteQuery_whenValidSql() {
        var request = QueryRequest.newBuilder()
            .setStatement(SQLStatement.newBuilder()
                .setSql("SELECT * FROM customers")
                .build())
            .build();
        
        var results = new ArrayList<QueryResultResponse>();
        stub.execQuery(request).forEachRemaining(results::add);
        
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getVector().getVectorSize()).isGreaterThan(0);
    }
}
```

### Test Configuration

**Test Resources**:
- `src/test/resources/application-test.yml`
- `src/test/resources/sql-scripts/test.sql`
- `src/test/resources/userstore/passwd.yml`

**Test Data**:
- Sample datasets in `test/datasets/`
  - `airlines/` - Flight data
  - `cmart/` - E-commerce data
  - `moneta/` - Banking data (primary test dataset)
  - `partitioned/` - Partitioned tables

**In-Memory Databases**:
- H2: `jdbc:h2:mem:test`
- HSQLDB: `jdbc:hsqldb:mem:test`

### Coverage

**Tool**: JaCoCo

**Threshold**: 0.8 (80% line coverage)

**Command**:
```bash
./gradlew test jacocoTestReport
```

**Report**: `build/reports/jacoco/test/html/index.html`

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :core:mill-core:test

# Specific test
./gradlew test --tests PolicyEvaluatorTest

# Integration tests
./gradlew testIT

# With coverage
./gradlew test jacocoTestReport
```

---

## Build System

### Gradle Configuration

**Version**: Gradle 8.x with Kotlin DSL

**Root Structure**:
- No root `settings.gradle.kts` (multi-repo, not mono-repo)
- Each top-level directory is a separate Gradle build
- Composite builds via `includeBuild()`

**Version Catalog**: `libs.versions.toml`

```toml
[versions]
springBoot = "3.5.4"
springAi = "1.0.3"
calcite = "1.40.0"
grpc = "1.74.0"
protobuf = "4.31.1"
substrait = "0.60.0"

[libraries]
boot-starter = { module = "org.springframework.boot:spring-boot-starter", version.ref = "springBoot" }
calcite-core = { module = "org.apache.calcite:calcite-core", version.ref = "calcite" }
grpc-stub = { module = "io.grpc:grpc-stub", version.ref = "grpc" }

[bundles]
jdbc-pack = [
    "h2-database",
    "drivers-postgressql",
    "drivers-mysql",
    "drivers-duckdb"
]

[plugins]
spring-boot-plugin = { id = "org.springframework.boot", version.ref = "springBoot" }
```

### Custom Plugins

**Location**: `build-logic/src/main/kotlin/`

**Plugins**:
1. `io.qpointz.plugins.mill`:
   - Common build conventions
   - Java/Kotlin configuration
   - Dependency management
   - Testing setup

2. `io.qpointz.plugins.mill-publish`:
   - Maven publishing configuration
   - Artifact metadata

**Usage in `build.gradle.kts`**:
```kotlin
plugins {
    id("io.qpointz.plugins.mill")
}
```

### Build Commands

```bash
# Build all modules
./gradlew build

# Clean build
./gradlew clean build

# Build specific module (from module directory)
cd core
./gradlew :mill-core:build

# Run tests
./gradlew test

# Run integration tests
./gradlew testIT

# Generate coverage report
./gradlew jacocoTestReport

# List tasks
./gradlew tasks

# Application tasks
./gradlew tasks --group application

# Dependency tree
./gradlew dependencies
```

### Java Toolchain

**Target**: Java 17+ (likely)

**Lombok**: Enabled project-wide
- `@Slf4j`, `@Getter`, `@Setter`, `@Builder`, `val`, etc.
- Lombok configuration: `misc/lombok.config`

**Compiler Options**:
- 4-space indentation
- UTF-8 encoding
- Annotation processing enabled

### Protobuf Generation

**Plugin**: `com.google.protobuf`

**Configuration**:
```kotlin
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.31.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.74.0"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}
```

**Generated Code**: Committed to repository under `proto/`

---

## Deployment

### Docker

**Containerization**: Services designed for containerized deployment

**Example Dockerfile** (typical structure):
```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 9099 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose** (likely in `misc/etc/docker/`):
```yaml
version: '3.8'

services:
  mill-grpc:
    image: mill-grpc-service
    ports:
      - "9099:9099"
    environment:
      - MILL_BACKEND_PROVIDER=jdbc
      - MILL_BACKEND_JDBC_URL=jdbc:postgresql://db:5432/mill
    depends_on:
      - db
  
  mill-http:
    image: mill-http-service
    ports:
      - "8080:8080"
    depends_on:
      - mill-grpc
  
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=mill
      - POSTGRES_USER=mill
      - POSTGRES_PASSWORD=mill
```

### Cloud Deployment

#### Azure

**Azure Functions** (`cloud/mill-azure-service-function/`):
- Spring Cloud Function adapter
- Serverless deployment
- HTTP triggers

**Configuration** (`host.json`):
```json
{
  "version": "2.0",
  "extensionBundle": {
    "id": "Microsoft.Azure.Functions.ExtensionBundle",
    "version": "[4.*, 5.0.0)"
  }
}
```

**Deployment**:
```bash
cd cloud/mill-azure-service-function
./gradlew azureFunctionsDeploy
```

#### Kubernetes

**Infrastructure** (`infra/terraform/azure-mill-aks/`):
- Terraform configurations for AKS
- Managed Kubernetes deployment
- Load balancers, ingress controllers

**Deployment** (typical):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mill-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: mill-service
  template:
    metadata:
      labels:
        app: mill-service
    spec:
      containers:
      - name: mill-service
        image: mill-service:latest
        ports:
        - containerPort: 9099
        - containerPort: 8080
        env:
        - name: MILL_BACKEND_PROVIDER
          value: jdbc
        - name: MILL_SECURITY_ENABLE
          value: "true"
```

### Configuration Management

**Environment Variables**:
```bash
# Backend
export MILL_BACKEND_PROVIDER=jdbc
export MILL_BACKEND_JDBC_URL=jdbc:postgresql://...
export MILL_BACKEND_JDBC_USERNAME=user
export MILL_BACKEND_JDBC_PASSWORD=pass

# Security
export MILL_SECURITY_ENABLE=true
export MILL_SECURITY_AUTHENTICATION_BASIC_FILE_STORE=file:/config/passwd.yml

# AI
export OPENAI_API_KEY=sk-...
export MILL_AI_NL2SQL_DIALECT=POSTGRESQL
```

**Spring Profiles**:
```bash
# Run with specific profile
java -jar app.jar --spring.profiles.active=production

# Multiple profiles
java -jar app.jar --spring.profiles.active=production,azure
```

**Config Files**:
- `application.yml` - Default configuration
- `application-{profile}.yml` - Profile-specific overrides
- External config: `--spring.config.location=/etc/mill/application.yml`

---

## Technology Stack

### Languages

| Language | Usage | Lines of Code (approx) |
|----------|-------|------------------------|
| **Java** | Core platform, services, AI | ~80% |
| **TypeScript** | Web UI (Grinder) | ~10% |
| **Kotlin** | Build scripts | ~5% |
| **Python** | Client library | ~3% |
| **Scala** | Spark connector | ~2% |

### Frameworks & Libraries

#### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.5.4 | Application framework |
| **Spring Security** | 6.5.2 | Authentication & authorization |
| **Spring AI** | 1.0.3 | LLM integration |
| **Apache Calcite** | 1.40.0 | Query engine |
| **Substrait** | 0.60.0 | Query plan IR |
| **gRPC** | 1.74.0 | RPC framework |
| **Protocol Buffers** | 4.31.1 | Serialization |
| **Jackson** | 2.19.2 | JSON processing |
| **Lombok** | 1.18.38 | Boilerplate reduction |

#### Testing

| Technology | Version | Purpose |
|------------|---------|---------|
| **JUnit** | 5.13.4 | Unit testing |
| **Mockito** | 5.18.0 | Mocking |
| **Spring Boot Test** | 3.5.4 | Integration testing |

#### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | ~18 | UI framework |
| **TypeScript** | ~5 | Type safety |
| **Vite** | ~5 | Build tool |

#### Databases (Drivers)

| Database | Version | Notes |
|----------|---------|-------|
| **PostgreSQL** | 42.7.7 | Primary production DB |
| **H2** | 2.3.232 | Embedded, testing |
| **DuckDB** | 1.3.2.0 | Analytics |
| **Trino** | 476 | Distributed SQL |
| **Snowflake** | 3.25.1 | Cloud warehouse |
| **ClickHouse** | 0.9.1 | OLAP |
| **MariaDB** | 3.5.5 | MySQL alternative |
| **SQLite** | 3.50.3.0 | Embedded |

### Architecture Patterns

- **Hexagonal Architecture**: Clear domain/infrastructure separation
- **Microservices**: Independent, deployable services
- **CQRS-lite**: Separate read/write paths for queries
- **Event-Driven**: Streaming query results
- **Repository Pattern**: Data access abstraction
- **Factory Pattern**: Context and provider creation
- **Strategy Pattern**: Backend and authentication strategies
- **Chain of Responsibility**: Authentication readers
- **Template Method**: Query execution flow

### Design Principles

- **SOLID Principles**: Single Responsibility, Open/Closed, etc.
- **Dependency Injection**: Spring-managed beans
- **Interface Segregation**: Small, focused interfaces
- **Immutability**: Record classes, `val` usage
- **Fail-Fast**: Validation at boundaries
- **Configuration over Code**: Externalized configuration

---

## Key Strengths & Areas for Enhancement

### Strengths

1. **Modular Architecture**:
   - Clean separation of concerns (core, services, AI, clients)
   - Composite Gradle builds for independent modules
   - Well-defined module boundaries

2. **Extensibility**:
   - Plugin architecture for backends
   - Pluggable authentication methods
   - Custom SQL function registration
   - Substrait-based query IR

3. **Modern Technology Stack**:
   - Spring Boot 3.x (latest)
   - gRPC for high-performance RPC
   - Protocol Buffers for efficient serialization
   - Spring AI for LLM integration

4. **AI/NL2SQL Sophistication**:
   - Two-phase intent resolution
   - Value mapping system with RAG
   - Multi-dialect support
   - Schema-aware query generation
   - Conversation memory

5. **Security**:
   - Multi-provider authentication (Basic, OAuth2, Entra ID)
   - Fine-grained authorization (row, column, schema levels)
   - Policy-based access control
   - Spring Security integration

6. **Data Model**:
   - Efficient columnar vector format
   - Streaming support for large result sets
   - Type-safe with logical/physical separation
   - JDBC compatibility

7. **Protocol-First Design**:
   - Well-defined protobuf contracts
   - Version negotiation (Handshake)
   - Backward compatibility

8. **Testing**:
   - Comprehensive unit tests
   - Integration test suite
   - Coverage tracking (JaCoCo)
   - Test fixtures and sample data

9. **Multi-Client Support**:
   - JDBC driver (Java ecosystem)
   - Python client (data science)
   - Spark connector (big data)

10. **Cloud-Native**:
    - Containerizable services
    - Azure Functions support
    - Kubernetes deployment
    - Infrastructure as Code (Terraform)

### Areas for Enhancement

1. **Documentation**:
   - Some docs are placeholder stubs (`docs/src/quickstart.md`)
   - API documentation could be more comprehensive
   - Architecture diagrams would be helpful
   - User guides for common scenarios

2. **Flow Module**:
   - Appears to be early-stage (`flow/`)
   - Limited to Excel and file system I/O
   - Could expand to more data sources

3. **Observability**:
   - Micrometer included, but limited instrumentation visible
   - Distributed tracing (OpenTelemetry) not obvious
   - Structured logging could be enhanced
   - Query performance metrics

4. **Caching**:
   - No obvious query result caching layer
   - Metadata caching could improve performance
   - LLM response caching for repeated questions

5. **Connection Pooling**:
   - Not explicitly configured in visible configs
   - HikariCP integration could be more obvious
   - Connection lifecycle management

6. **Query Optimization**:
   - Predicate pushdown visibility
   - Cost-based optimization hints
   - Query plan explanation for users

7. **Error Handling**:
   - Exception hierarchy could be more granular
   - User-friendly error messages in AI responses
   - Retry strategies for transient failures

8. **Schema Evolution**:
   - Handling schema changes over time
   - Versioning for metadata
   - Migration strategies

9. **Multi-Tenancy**:
   - Tenant isolation strategies
   - Per-tenant configuration
   - Resource quotas

10. **Rate Limiting**:
    - API rate limiting not obvious
    - LLM API quota management
    - Query cost controls

### Recommended Next Steps

**For New Contributors**:
1. Read `AGENTS.md` for repository guidelines
2. Set up local development environment
3. Run existing tests: `./gradlew test`
4. Try quickstart with Moneta dataset
5. Explore gRPC service with gRPCurl or Python client

**For Production Deployment**:
1. Configure production database (PostgreSQL recommended)
2. Set up SSL/TLS for gRPC and HTTP
3. Configure OAuth2/JWT authentication
4. Define authorization policies
5. Set up monitoring (Prometheus + Grafana)
6. Configure LLM provider (OpenAI or Azure OpenAI)
7. Deploy to Kubernetes or Azure Functions
8. Set up CI/CD pipeline

**For Feature Development**:
1. Add new backend: Implement `ExecutionProvider`, `SchemaProvider`
2. Add authentication method: Implement `AuthenticationMethod`
3. Add SQL function: Extend `ExtensionCollector`
4. Add NL2SQL intent: Create template in `templates/nlsql/intent/`
5. Add dialect support: Create dialect configuration in `templates/nlsql/dialects/`

---

## Conclusion

Mill is a well-architected, modern data integration platform with sophisticated AI capabilities. Its modular design, use of industry standards (Substrait, gRPC, Protobuf), and extensibility make it suitable for a wide range of data access scenarios. The natural language to SQL conversion is particularly advanced, with intent reasoning, value mapping, and multi-dialect support.

The platform is production-ready for:
- **Data Federation**: Querying across multiple sources
- **NL Analytics**: Business users querying data with plain English
- **Data Governance**: Fine-grained security policies
- **Embedded Analytics**: JDBC driver for application integration

With continued development in documentation, observability, and caching, Mill could become a powerful tool in the modern data stack.

---

**Document Version**: 1.0  
**Last Updated**: November 5, 2025  
**Generated By**: AI-powered codebase analysis

