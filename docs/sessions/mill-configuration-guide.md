# Mill Configuration Guide

This guide explains all Mill configuration keys and what each setting controls.

## Quick Start

Add this to your `application.yml` file to get started:

```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:h2:mem:test"
      driver: "org.h2.Driver"
  services:
    grpc:
      enable: true
      port: 9099
    jet-http:
      enable: true
```

## Configuration Keys Reference

### Backend Configuration

#### `mill.backend.provider`
**What it controls:** Which data backend to use
- `jdbc` - Connect to any JDBC-compatible database (PostgreSQL, MySQL, H2, etc.)
- `calcite` - Use Apache Calcite for query processing

#### `mill.backend.connection.quoting`
**What it controls:** How database identifiers (table names, column names) are quoted
- `BACK_TICK` - Use backticks: `table_name`
- `DOUBLE_QUOTE` - Use double quotes: "table_name"

#### `mill.backend.connection.caseSensitive`
**What it controls:** Whether database identifiers are case-sensitive
- `true` - Table names like "Users" and "users" are different
- `false` - Table names like "Users" and "users" are the same

#### `mill.backend.connection.unquotedCasing`
**What it controls:** How unquoted identifiers are handled
- `UNCHANGED` - Keep identifiers as written
- `UPPER` - Convert to uppercase
- `LOWER` - Convert to lowercase

#### `mill.backend.connection.fun`
**What it controls:** Function naming convention
- `oracle` - Use Oracle-style function names

#### `mill.backend.connection.conformance`
**What it controls:** SQL standard compliance level
- `ORACLE_12` - Follow Oracle 12c SQL standards

#### `mill.backend.connection.model`
**What it controls:** Path to Calcite model file (only for Calcite provider)
- Example: `./config/model.yaml`

### JDBC Configuration

#### `mill.backend.jdbc.url`
**What it controls:** Database connection URL
- Examples:
  - `jdbc:h2:mem:test` - H2 in-memory database
  - `jdbc:postgresql://localhost:5432/mydb` - PostgreSQL database
  - `jdbc:mysql://localhost:3306/mydb` - MySQL database

#### `mill.backend.jdbc.driver`
**What it controls:** JDBC driver class name
- `org.h2.Driver` - H2 database driver
- `org.postgresql.Driver` - PostgreSQL driver
- `com.mysql.cj.jdbc.Driver` - MySQL driver

#### `mill.backend.jdbc.username`
**What it controls:** Database username
- `*` - Use any username (for testing)
- `myuser` - Specific username

#### `mill.backend.jdbc.password`
**What it controls:** Database password
- `*` - Use any password (for testing)
- `mypass` - Specific password

#### `mill.backend.jdbc.target-schema`
**What it controls:** Default schema for queries
- `PUBLIC` - Use PUBLIC schema
- `MYSCHEMA` - Use specific schema

#### `mill.backend.jdbc.output-schema`
**What it controls:** Schema for output results
- `RESULTS` - Store results in RESULTS schema
- `TEMP` - Store results in TEMP schema

#### `mill.backend.jdbc.multi-schema`
**What it controls:** Enable multi-schema support
- `true` - Allow queries across multiple schemas
- `false` - Single schema only

### Security Configuration

#### `mill.security.enable`
**What it controls:** Enable or disable security features
- `true` - Require authentication and authorization
- `false` - Allow anonymous access

#### `mill.security.authentication.basic.enable`
**What it controls:** Enable basic username/password authentication
- `true` - Require username and password
- `false` - Disable basic auth

#### `mill.security.authentication.basic.file-store`
**What it controls:** Where to find user credentials
- `file:./config/passwd.yml` - File path
- `classpath:passwd.yml` - Classpath resource

#### `mill.security.authentication.oauth2-resource-server.enable`
**What it controls:** Enable OAuth2 JWT token validation
- `true` - Validate JWT tokens
- `false` - Disable OAuth2

#### `mill.security.authentication.oauth2-resource-server.jwt.jwk-set-uri`
**What it controls:** Where to get JWT signing keys
- `https://your-auth-provider.com/.well-known/jwks.json` - JWK Set URL

#### `mill.security.authentication.entra-id-token.enable`
**What it controls:** Enable Microsoft Entra ID token validation
- `true` - Validate Entra ID tokens
- `false` - Disable Entra ID auth

#### `mill.security.authorization.policy.enable`
**What it controls:** Enable policy-based authorization
- `true` - Use policies to control access
- `false` - Disable policy authorization

#### `mill.security.authorization.policy.actions[].policy`
**What it controls:** Policy name for authorization rules
- `admin` - Admin policy
- `user` - User policy
- `readonly` - Read-only policy

#### `mill.security.authorization.policy.actions[].verb`
**What it controls:** What the policy allows or denies
- `allow` - Allow the action
- `deny` - Deny the action

#### `mill.security.authorization.policy.actions[].action`
**What it controls:** What action the policy applies to
- `rel-read` - Read table data
- `rel-filter` - Filter table data
- `rel-write` - Write table data

### Services Configuration

#### `mill.services.grpc.enable`
**What it controls:** Enable gRPC service
- `true` - Start gRPC server
- `false` - Disable gRPC service

#### `mill.services.grpc.port`
**What it controls:** Port number for gRPC service
- `9099` - Default port
- `8080` - Alternative port

#### `mill.services.grpc.address`
**What it controls:** Network address for gRPC service
- `*` - Listen on all interfaces
- `localhost` - Listen only on localhost

#### `mill.services.jet-http.enable`
**What it controls:** Enable HTTP service
- `true` - Start HTTP server
- `false` - Disable HTTP service

#### `mill.services.ai-nl2data.enable`
**What it controls:** Enable AI natural language to data service
- `true` - Allow natural language queries
- `false` - Disable AI features

#### `mill.services.grinder.enable`
**What it controls:** Enable data processing service
- `true` - Enable data grinding/processing
- `false` - Disable data processing

#### `mill.services.meta.enable`
**What it controls:** Enable metadata service
- `true` - Provide metadata information
- `false` - Disable metadata service

#### `mill.services.data-bot.enable`
**What it controls:** Enable AI data bot service
- `true` - Enable conversational data bot
- `false` - Disable data bot

#### `mill.services.data-bot.prompt-file`
**What it controls:** Path to AI prompt file
- `file:./config/prompts/bot.prompt` - File path
- `classpath:prompts/bot.prompt` - Classpath resource

#### `mill.services.data-bot.model-name`
**What it controls:** AI model to use for data bot
- `gpt-4` - OpenAI GPT-4 model
- `gpt-3.5-turbo` - OpenAI GPT-3.5 model

### AI Configuration

#### `mill.ai.chat.memory`
**What it controls:** How to store chat memory
- `in-memory` - Store in memory (lost on restart)
- `jdbc` - Store in database (persistent)

#### `mill.ai.nl2sql.enable`
**What it controls:** Enable natural language to SQL conversion
- `true` - Convert natural language to SQL
- `false` - Disable NL2SQL

#### `mill.ai.nl2sql.dialect`
**What it controls:** SQL dialect for AI queries
- `H2` - H2 database dialect
- `POSTGRESQL` - PostgreSQL dialect
- `MYSQL` - MySQL dialect

#### `mill.ai.metadata.relations`
**What it controls:** How to get relation metadata
- `none` - No relation metadata
- `file` - Read from file

#### `mill.ai.metadata.annotations`
**What it controls:** How to get annotation metadata
- `none` - No annotation metadata
- `file` - Read from file

#### `mill.ai.metadata.file.repository.path`
**What it controls:** Path to metadata file
- `file:./config/metadata.yaml` - File path

### Metadata Configuration

#### `mill.metadata.relations`
**What it controls:** Source of relation metadata
- `none` - No relation metadata
- `file` - Read from file

#### `mill.metadata.annotations`
**What it controls:** Source of annotation metadata
- `none` - No annotation metadata
- `file` - Read from file

#### `mill.metadata.file.repository.path`
**What it controls:** Path to metadata repository file
- `file:./config/metadata.yaml` - File path

## Common Configuration Examples

### Basic Setup (No Security)
```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:h2:mem:test"
      driver: "org.h2.Driver"
  services:
    grpc:
      enable: true
      port: 9099
    jet-http:
      enable: true
```

### Production Setup with Security
```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:postgresql://localhost:5432/milldb"
      driver: "org.postgresql.Driver"
      username: "milluser"
      password: "millpass"
      target-schema: "mill"
  security:
    enable: true
    authentication:
      oauth2-resource-server:
        enable: true
        jwt:
          jwk-set-uri: "https://your-auth-provider.com/.well-known/jwks.json"
  services:
    grpc:
      enable: true
      port: 9099
    jet-http:
      enable: true
```

### AI-Enabled Setup
```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:postgresql://localhost:5432/milldb"
      driver: "org.postgresql.Driver"
  ai:
    chat:
      memory: jdbc
    nl2sql:
      enable: true
      dialect: POSTGRESQL
  services:
    ai-nl2data:
      enable: true
    data-bot:
      enable: true
      model-name: "gpt-4"
```

### Calcite Setup (File-based Data)
```yaml
mill:
  backend:
    provider: calcite
    connection:
      model: ./config/data-model.yaml
  services:
    grpc:
      enable: true
      port: 9099
```

## Configuration Profiles

Use Spring profiles to switch between different configurations:

```yaml
# Default profile
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:h2:mem:test"

---
# Production profile
spring:
  config:
    activate:
      on-profile: production
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:postgresql://prod-server:5432/milldb"
  security:
    enable: true
```

## Tips

1. **Start Simple**: Begin with basic JDBC setup and add features gradually
2. **Use Profiles**: Different configurations for development, testing, and production
3. **Security**: Always enable security in production environments
4. **Database**: Choose the right database driver for your database
5. **AI Features**: Only enable AI services if you need them (they require additional setup)
