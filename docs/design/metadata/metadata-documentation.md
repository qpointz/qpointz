# Mill Metadata System - User Documentation

**Version:** 1.0  
**Last Updated:** December 2024  
**Audience:** Users, Administrators, Developers

---

## Table of Contents

1. [Introduction](#introduction)
2. [What is Metadata?](#what-is-metadata)
3. [Key Concepts](#key-concepts)
4. [Using the Metadata Browser](#using-the-metadata-browser)
5. [Metadata Facets](#metadata-facets)
6. [Configuration](#configuration)
7. [Best Practices](#best-practices)
8. [FAQ](#faq)

---

## Introduction

The Mill Metadata System is a centralized platform for managing information about your data assets. It helps you:

- **Document your data**: Add descriptions, business meanings, and tags to tables and columns
- **Improve AI understanding**: Enhance natural language query accuracy with value mappings and business context
- **Navigate your schema**: Browse and search your database structure through an intuitive interface
- **Share knowledge**: Create shareable links to specific tables or columns for collaboration

The system uses a **faceted architecture**, meaning each data asset (table, column, etc.) can have multiple types of metadata attached to it, such as descriptions, relationships, value mappings, and more.

---

## What is Metadata?

Metadata is "data about data." In the context of Mill, metadata includes:

- **Structural information**: Table names, column names, data types, constraints
- **Descriptive information**: What a table or column represents in business terms
- **Relationships**: How tables connect to each other
- **Value mappings**: How user-friendly terms map to database values (e.g., "premium" → "PREMIUM")
- **Business concepts**: High-level concepts that span multiple tables

### Why is Metadata Important?

1. **Better AI Queries**: When you ask "show me premium customers," the system knows that "premium" maps to the database value "PREMIUM"
2. **Self-Documentation**: Your data becomes self-explanatory with descriptions and business context
3. **Collaboration**: Team members can understand data assets without deep database knowledge
4. **Data Governance**: Track ownership, classification, and usage of data assets

---

## Key Concepts

### Entities

An **entity** represents a data asset in your system. There are four types:

- **Schema**: A logical grouping of tables (e.g., `moneta`, `sales`)
- **Table**: A database table (e.g., `clients`, `orders`)
- **Attribute**: A column in a table (e.g., `client_id`, `first_name`)
- **Concept**: A business concept that spans multiple tables (e.g., "Premium Customers")

Each entity has a unique identifier (ID) in the format `schema.table` or `schema.table.attribute`.

### Facets

A **facet** is one aspect or dimension of metadata. Think of facets as different "views" of the same entity:

- **Descriptive Facet**: Human-readable information (names, descriptions, tags)
- **Structural Facet**: Physical database details (data types, constraints)
- **Relation Facet**: Relationships between tables
- **Value Mapping Facet**: User term → database value mappings
- **Concept Facet**: Business concepts spanning multiple tables

Each entity can have multiple facets, and each facet can have multiple **scopes** (see below).

### Scopes

A **scope** determines who can see a particular piece of metadata:

- **`global`**: Visible to everyone (default)
- **`user:{userId}`**: Visible only to a specific user (e.g., `user:alice@company.com`)
- **`team:{teamName}`**: Visible to team members (e.g., `team:engineering`)
- **`role:{roleName}`**: Visible to users with a role (e.g., `role:admin`)

When viewing metadata, the system automatically merges facets in priority order: user > team > role > global.

### Example

```yaml
# Entity: moneta.clients.client_id
facets:
  descriptive:
    global:                    # ← Scope: everyone sees this
      displayName: "Client ID"
      description: "Unique identifier for clients"
    user:alice@company.com:    # ← Scope: only Alice sees this
      description: "My personal notes about client IDs"
      notes: "Used in reporting"
```

---

## Using the Metadata Browser

The Metadata Browser is accessible at `/explore` in the Mill UI. It provides a visual interface for navigating and viewing metadata.

### Navigation

1. **Sidebar**: Shows a tree view of schemas → tables → attributes
   - Click a schema to expand/collapse
   - Click a table to see its columns
   - Click an attribute to view its details

2. **Entity Details**: When you select an entity, the main panel shows:
   - Entity information (ID, type, location)
   - Facets in tabs (Descriptive, Structural, Relations, etc.)
   - Scope selector to view different scopes

3. **URL Sharing**: The URL updates as you navigate (e.g., `/explore/moneta/clients/client_id`), allowing you to:
   - Bookmark specific entities
   - Share links with team members
   - Use browser back/forward navigation

### Searching

Use the search bar to find entities by:
- Name (table name, column name)
- Description text
- Tags
- Business domain

---

## Metadata Facets

### Descriptive Facet

**Purpose**: Human-readable information about entities.

**Common Fields**:
- `displayName`: User-friendly name (e.g., "Client ID" instead of "CLIENT_ID")
- `description`: Short description of what the entity represents
- `businessMeaning`: Detailed explanation for AI/automation
- `synonyms`: Alternative names (e.g., ["customer", "client"])
- `tags`: Categorization tags (e.g., ["pii", "primary-key"])
- `businessDomain`: Domain classification (e.g., "customer-management")
- `owner`: Responsible team/person
- `classification`: Data classification (PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED)

**Example**:
```yaml
descriptive:
  global:
    displayName: "Customer ID"
    description: "Unique identifier for customer records"
    businessMeaning: "Primary key used across all customer-related systems"
    synonyms: [customer number, client id]
    tags: [pii, primary-key]
    businessDomain: customer-management
    owner: crm-team@company.com
    classification: CONFIDENTIAL
```

### Structural Facet

**Purpose**: Physical database schema information.

**Common Fields**:
- `physicalName`: Actual database name (e.g., "CLIENT_ID")
- `physicalType`: Data type (e.g., "VARCHAR", "INTEGER")
- `nullable`: Whether NULL values are allowed
- `isPrimaryKey`, `isForeignKey`, `isUnique`: Constraint flags
- `backendType`: Backend type (e.g., "jdbc", "calcite")

**Example**:
```yaml
structural:
  global:
    physicalName: CLIENT_ID
    physicalType: INTEGER
    nullable: false
    isPrimaryKey: true
    backendType: jdbc
```

### Relation Facet

**Purpose**: Defines relationships between tables.

**Common Fields**:
- `relations[]`: List of relationships
  - `name`: Relationship name
  - `description`: Explanation
  - `sourceTable`, `targetTable`: Related tables
  - `cardinality`: ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY
  - `type`: FOREIGN_KEY, LOGICAL, HIERARCHICAL
  - `joinSql`: SQL join expression

**Example**:
```yaml
relation:
  global:
    relations:
      - name: customer_accounts
        description: "Customer to accounts relationship"
        sourceTable:
          schema: moneta
          table: customers
        targetTable:
          schema: moneta
          table: accounts
        cardinality: ONE_TO_MANY
        type: FOREIGN_KEY
        joinSql: "customers.customer_id = accounts.customer_id"
```

### Value Mapping Facet

**Purpose**: Maps user-friendly terms to database values for natural language queries.

**Common Fields**:
- `mappings[]`: List of value mappings
  - `userTerm`: What users say (e.g., "premium")
  - `databaseValue`: Actual DB value (e.g., "PREMIUM")
  - `displayValue`: UI display (e.g., "Premium")
  - `language`: ISO 639-1 code (e.g., "en", "es")
  - `aliases`: Alternative terms
- `sources[]`: Dynamic mapping sources (SQL queries, APIs)

**Example**:
```yaml
value-mapping:
  global:
    context: Client segment
    mappings:
      - userTerm: premium
        databaseValue: PREMIUM
        displayValue: Premium
        language: en
        aliases: [gold, vip]
      - userTerm: básico
        databaseValue: BASIC
        displayValue: Básico
        language: es
    sources:
      - type: SQL_QUERY
        name: segment_values
        definition: "SELECT DISTINCT segment FROM clients"
        enabled: true
```

**How It Works**:
When a user asks "show me premium customers," the system:
1. Looks up value mappings for the `segment` column
2. Finds that "premium" maps to "PREMIUM"
3. Generates SQL: `SELECT * FROM clients WHERE segment = 'PREMIUM'`

### Concept Facet

**Purpose**: Defines business concepts that span multiple tables.

**Common Fields**:
- `concepts[]`: List of concepts
  - `name`: Concept name (e.g., "Premium Customers")
  - `description`: Explanation
  - `sql`: SQL definition (optional)
  - `targets[]`: Tables/columns this concept relates to
  - `tags`, `category`: Organization

**Example**:
```yaml
concept:
  global:
    concepts:
      - name: Premium Customers
        description: "High-value customer segment"
        sql: "segment = 'PREMIUM' AND balance > 100000"
        targets:
          - schema: moneta
            table: customers
          - schema: moneta
            table: accounts
            attributes: [segment, balance]
        tags: [segmentation, marketing]
        category: segmentation
```

---

## Configuration

### Basic Configuration

The metadata system is configured via `application.yml`:

```yaml
mill:
  metadata:
    v2:
      file:
        # Single file
        path: "classpath:metadata/example.yml"
        
        # Multiple files (comma-separated)
        path: "classpath:metadata/base.yml,classpath:metadata/overrides.yml"
        
        # File patterns (glob)
        path: "classpath:metadata/base.yml,classpath:metadata/overrides/*.yml"
```

### Multi-file Configuration

You can organize metadata across multiple files:

```yaml
# base.yml - Core metadata
entities:
  - id: moneta.clients
    facets:
      structural:
        global:
          physicalName: CLIENTS
      descriptive:
        global:
          displayName: Clients

# overrides.yml - Team-specific overrides
entities:
  - id: moneta.clients
    facets:
      descriptive:
        global:
          displayName: Customer Accounts  # Overrides base
        team:engineering:
          owner: engineering-team@company.com  # New scope
```

**Merging Rules**:
- Files are loaded in order (left to right)
- Entities with same ID: later files replace earlier entities
- Facets: Later files replace facets by type+scope (not merged within facet)
- Facets from earlier files are preserved if not present in later files

---

## Best Practices

### 1. Organize Metadata Files

- **Base files**: Core structural and descriptive metadata
- **Override files**: Team-specific or environment-specific metadata
- **Value mapping files**: Separate file for value mappings
- Use descriptive file names: `base.yml`, `overrides-production.yml`, `value-mappings.yml`

### 2. Use Scopes Appropriately

- **Global scope**: Use for metadata visible to everyone
- **User scope**: Use for personal notes or annotations
- **Team scope**: Use for team-specific documentation
- **Role scope**: Use for role-based metadata (e.g., admin-only)

### 3. Write Clear Descriptions

- **Description**: Short, one-sentence explanation
- **Business Meaning**: Detailed explanation for AI/automation
- **Use plain language**: Avoid technical jargon when possible

### 4. Tag Consistently

- Use consistent tag names across entities
- Create a tag taxonomy (e.g., `pii`, `financial`, `customer-data`)
- Use tags for filtering and organization

### 5. Maintain Value Mappings

- Keep value mappings up to date
- Add aliases for common variations
- Use language codes for internationalization
- Test mappings with actual user queries

### 6. Document Relationships

- Document all foreign key relationships
- Add business context to relationships
- Include join SQL for complex relationships

---

## FAQ

### Q: How do I add metadata to a new table?

A: Create or edit a YAML file with the entity definition:

```yaml
entities:
  - id: moneta.new_table
    type: TABLE
    schemaName: moneta
    tableName: new_table
    facets:
      descriptive:
        global:
          displayName: "New Table"
          description: "Description of the table"
```

### Q: Can I have different metadata for different environments?

A: Yes! Use multiple files:

```yaml
mill:
  metadata:
    v2:
      file:
        path: "classpath:metadata/base.yml,classpath:metadata/overrides-${spring.profiles.active}.yml"
```

### Q: How do value mappings work with natural language queries?

A: When you ask "show me premium customers," the system:
1. Identifies the relevant column (e.g., `segment`)
2. Looks up value mappings for that column
3. Finds that "premium" maps to "PREMIUM"
4. Generates SQL with the mapped value

### Q: Can I add custom metadata?

A: Yes! The faceted architecture allows adding new facet types. Contact your administrator or developer to add custom facets.

### Q: How do I share a link to a specific table or column?

A: Navigate to the entity in the Metadata Browser. The URL will be in the format `/explore/{schema}/{table}/{attribute?}`. Copy and share this URL.

### Q: What happens if I have the same entity in multiple files?

A: Later files replace entities with the same ID. Facets are merged by type+scope (later files replace facets completely, not merged within facet).

### Q: Can I edit metadata through the UI?

A: Currently, the UI is read-only. Editing is planned for a future release. For now, edit YAML files directly.

### Q: How do I search for entities?

A: Use the search bar in the Metadata Browser. You can search by:
- Entity name
- Description text
- Tags
- Business domain

---

## Getting Help

- **Documentation**: See `metadata-service-design.md` for technical details
- **API Documentation**: Access Swagger UI at `/swagger-ui.html` when the service is running
- **Support**: Contact your system administrator or development team

---

**Document Version:** 1.0  
**Last Updated:** December 2024

