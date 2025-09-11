# Mill Configuration Quick Reference

## Essential Settings

| Key | Values | What it does |
|-----|--------|--------------|
| `mill.backend.provider` | `jdbc`, `calcite` | Choose data backend |
| `mill.backend.jdbc.url` | `jdbc:h2:mem:test` | Database connection |
| `mill.backend.jdbc.driver` | `org.h2.Driver` | Database driver |
| `mill.services.grpc.enable` | `true`, `false` | Enable gRPC API |
| `mill.services.grpc.port` | `9099` | gRPC port number |
| `mill.services.jet-http.enable` | `true`, `false` | Enable HTTP API |
| `mill.security.enable` | `true`, `false` | Enable security |

## Backend Settings

| Key | Values | Effect |
|-----|--------|--------|
| `mill.backend.connection.quoting` | `BACK_TICK`, `DOUBLE_QUOTE` | How to quote identifiers |
| `mill.backend.connection.caseSensitive` | `true`, `false` | Case sensitivity for names |
| `mill.backend.connection.unquotedCasing` | `UNCHANGED`, `UPPER`, `LOWER` | Transform unquoted names |
| `mill.backend.jdbc.username` | `*`, `myuser` | Database username |
| `mill.backend.jdbc.password` | `*`, `mypass` | Database password |
| `mill.backend.jdbc.target-schema` | `PUBLIC`, `MYSCHEMA` | Default schema |

## Security Settings

| Key | Values | Effect |
|-----|--------|--------|
| `mill.security.authentication.basic.enable` | `true`, `false` | Username/password auth |
| `mill.security.authentication.basic.file-store` | `file:./passwd.yml` | User credentials file |
| `mill.security.authentication.oauth2-resource-server.enable` | `true`, `false` | OAuth2 JWT validation |
| `mill.security.authentication.entra-id-token.enable` | `true`, `false` | Microsoft Entra ID auth |

## Service Settings

| Key | Values | Effect |
|-----|--------|--------|
| `mill.services.grpc.address` | `*`, `localhost` | gRPC network binding |
| `mill.services.ai-nl2data.enable` | `true`, `false` | Natural language queries |
| `mill.services.grinder.enable` | `true`, `false` | Data processing |
| `mill.services.meta.enable` | `true`, `false` | Metadata service |
| `mill.services.data-bot.enable` | `true`, `false` | AI data bot |

## AI Settings

| Key | Values | Effect |
|-----|--------|--------|
| `mill.ai.chat.memory` | `in-memory`, `jdbc` | Chat memory storage |
| `mill.ai.nl2sql.enable` | `true`, `false` | Natural language to SQL |
| `mill.ai.nl2sql.dialect` | `H2`, `POSTGRESQL`, `MYSQL` | SQL dialect for AI |
| `mill.services.data-bot.model-name` | `gpt-4`, `gpt-3.5-turbo` | AI model for data bot |

## Common Database URLs

| Database | URL Example |
|----------|-------------|
| H2 (memory) | `jdbc:h2:mem:test` |
| H2 (file) | `jdbc:h2:file:./data/test` |
| PostgreSQL | `jdbc:postgresql://localhost:5432/mydb` |
| MySQL | `jdbc:mysql://localhost:3306/mydb` |
| SQL Server | `jdbc:sqlserver://localhost:1433;databaseName=mydb` |

## Common Drivers

| Database | Driver Class |
|----------|--------------|
| H2 | `org.h2.Driver` |
| PostgreSQL | `org.postgresql.Driver` |
| MySQL | `com.mysql.cj.jdbc.Driver` |
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |

## Quick Examples

### Minimal Setup
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
```

### With Security
```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:postgresql://localhost:5432/mydb"
      driver: "org.postgresql.Driver"
      username: "user"
      password: "pass"
  security:
    enable: true
    authentication:
      basic:
        enable: true
        file-store: "file:./users.yml"
  services:
    grpc:
      enable: true
    jet-http:
      enable: true
```

### With AI Features
```yaml
mill:
  backend:
    provider: jdbc
    jdbc:
      url: "jdbc:postgresql://localhost:5432/mydb"
      driver: "org.postgresql.Driver"
  ai:
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
