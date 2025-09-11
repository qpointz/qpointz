# Value Mapping: Static + Dynamic (SQL) Sources

**Status:** ✅ Implemented  
**Purpose:** Support both manual value mappings AND SQL-generated mappings for RAG

---

## Overview

The system now supports **two types** of value mappings in the same metadata file:

1. **Static Mappings** - Manually defined (acronyms, fixed values, multi-language)
2. **Dynamic SQL Sources** - Generated from database queries (distinct values, reference tables)

Both are automatically ingested into RAG at startup.

---

## YAML Format

### Complete Example

```yaml
schemas:
  - name: MONETA
    tables:
      - name: CLIENTS
        attributes:
          # Example 1: Static mappings only (acronyms, aliases)
          - name: SEGMENT
            type: VARCHAR
            value-mappings:
              mappings:
                - user-term: premium
                  database-value: PREMIUM
                  language: en
                  aliases: [gold, vip]
              
              sources: []  # No SQL sources
          
          # Example 2: Both static and dynamic
          - name: COUNTRY
            type: VARCHAR
            value-mappings:
              # Static: Common acronyms
              mappings:
                - user-term: USA
                  database-value: US
                  aliases: [United States, America]
                
                - user-term: UK
                  database-value: GB
                  aliases: [United Kingdom]
              
              # Dynamic: All values from database
              sources:
                - type: sql
                  name: country_distinct_values
                  description: Get all countries from database
                  enabled: true
                  sql: |
                    SELECT DISTINCT
                      "COUNTRY" AS "ID",
                      "COUNTRY" AS "VALUE",
                      "COUNTRY" AS "TEXT"
                    FROM "MONETA"."CLIENTS"
                    WHERE "COUNTRY" IS NOT NULL
                  cache-ttl-seconds: 3600
          
          # Example 3: SQL sources only (no static)
          - name: STATUS
            type: VARCHAR
            value-mappings:
              mappings: []  # No static mappings
              
              sources:
                - type: sql
                  name: status_values
                  enabled: true
                  sql: |
                    SELECT DISTINCT
                      "STATUS" AS "ID",
                      "STATUS" AS "VALUE",
                      "STATUS" AS "TEXT"
                    FROM "MONETA"."CLIENTS"
```

---

## SQL Query Format

### Required Columns

SQL queries **must return exactly 3 columns**:

```sql
SELECT 
  column AS "ID",      -- Unique identifier (used in document ID)
  column AS "VALUE",   -- Database value (what goes into SQL)
  column AS "TEXT"     -- Display text (for RAG embedding)
FROM table
WHERE column IS NOT NULL
```

### Examples

#### Simple Distinct Values

```sql
SELECT DISTINCT
  "SEGMENT" AS "ID",
  "SEGMENT" AS "VALUE",
  "SEGMENT" AS "TEXT"
FROM "MONETA"."CLIENTS"
WHERE "SEGMENT" IS NOT NULL
```

**Result:**
```
ID      | VALUE    | TEXT
--------|----------|----------
PREMIUM | PREMIUM  | PREMIUM
STANDARD| STANDARD | STANDARD
BASIC   | BASIC    | BASIC
```

#### With Display Names from Reference Table

```sql
SELECT 
  c.code AS "ID",
  c.code AS "VALUE",
  c.name AS "TEXT"
FROM reference.countries c
ORDER BY c.name
```

**Result:**
```
ID | VALUE | TEXT
---|-------|----------------
US | US    | United States
GB | GB    | United Kingdom
CA | CA    | Canada
```

#### With Descriptions for Better RAG

```sql
SELECT 
  "STATUS" AS "ID",
  "STATUS" AS "VALUE",
  "STATUS" || ' - ' || "STATUS_DESCRIPTION" AS "TEXT"
FROM "MONETA"."CLIENT_STATUS_TYPES"
```

**Result:**
```
ID     | VALUE  | TEXT
-------|--------|-------------------------
ACTIVE | ACTIVE | ACTIVE - Currently active
CLOSED | CLOSED | CLOSED - Account closed
```

---

## How It Works

### Startup Process

```
Application starts
  ↓
ValueMappingComponents.onApplicationReady()
  ↓
┌────────────────────────────────┐
│ 1. Ingest Static Mappings      │
│────────────────────────────────│
│ metadataProvider.getAllValueMappings()
│   Returns: All manual mappings
│   Creates: RAG documents with toEmbeddingText()
│   Ingests: Into vector store
└────────────────────────────────┘
  ↓
┌────────────────────────────────┐
│ 2. Ingest Dynamic Mappings     │
│────────────────────────────────│
│ metadataProvider.getAllValueMappingSources()
│   Returns: All SQL sources
│   For each enabled source:
│     - Execute SQL query
│     - Process results (ID, VALUE, TEXT)
│     - Create RAG documents
│     - Ingest into vector store
└────────────────────────────────┘
  ↓
Ready for queries!
```

### Log Output

```
INFO  Value Mapping RAG component initialized.
INFO  Ingesting static value mappings from metadata...
INFO  Found 12 static value mappings to ingest
DEBUG Created static document: premium -> PREMIUM
DEBUG Created static document: gold -> PREMIUM
...
INFO  Ingesting 12 static documents into RAG vector store
INFO  Successfully ingested static value mappings

INFO  Ingesting dynamic value mappings from SQL sources...
INFO  Processing SQL source: country_distinct_values for MONETA.CLIENTS.COUNTRY
INFO  Ingested 45 documents from source: country_distinct_values
INFO  Processing SQL source: status_values for MONETA.CLIENTS.STATUS
INFO  Ingested 5 documents from source: status_values
INFO  Successfully ingested 50 dynamic documents from 2 SQL sources
```

---

## Use Cases

### Static Mappings - Use When:

✅ **Acronyms** - USA → US, UK → GB  
✅ **Synonyms** - premium/gold/vip → PREMIUM  
✅ **Multi-language** - básico (es) → BASIC  
✅ **Fixed values** - Well-known values that don't change  
✅ **Aliases** - Multiple terms for same value  

### Dynamic SQL Sources - Use When:

✅ **Many values** - Countries, cities, product names (100s of values)  
✅ **Changing data** - Status codes, categories that evolve  
✅ **Reference tables** - Lookup tables with display names  
✅ **Data-driven** - Values discovered from actual data  
✅ **Computed text** - Combining multiple columns for better search  

### Combined - Use When:

✅ **Common + Rare** - Static for common terms, SQL for all values  
✅ **Acronyms + Full** - Static for "USA", SQL for "United States of America"  
✅ **Best of both** - Manual curation + automatic discovery  

---

## Example Configurations

### Example 1: Country (Static Acronyms + Dynamic Full List)

```yaml
- name: COUNTRY
  value-mappings:
    # Static: Common acronyms
    mappings:
      - user-term: USA
        database-value: US
        aliases: [America, United States]
      
      - user-term: UK  
        database-value: GB
        aliases: [Britain, United Kingdom]
    
    # Dynamic: All countries from DB
    sources:
      - type: sql
        name: all_countries
        enabled: true
        sql: |
          SELECT DISTINCT
            "COUNTRY" AS "ID",
            "COUNTRY" AS "VALUE",
            "COUNTRY_NAME" AS "TEXT"
          FROM "REFERENCE"."COUNTRIES"
```

**Result:** 
- Static: "USA", "America", "United States" → US
- Dynamic: All country codes + names from reference table

### Example 2: Product Codes (Dynamic Only)

```yaml
- name: PRODUCT_CODE
  value-mappings:
    mappings: []  # No static mappings
    
    sources:
      - type: sql
        name: products_with_names
        enabled: true
        sql: |
          SELECT
            p.code AS "ID",
            p.code AS "VALUE",
            p.code || ' - ' || p.name || ' (' || c.category_name || ')' AS "TEXT"
          FROM products p
          JOIN categories c ON p.category_id = c.id
          WHERE p.active = TRUE
```

**Result:** RAG documents with rich text like "PRD001 - Premium Widget (Electronics)"

### Example 3: Status (Static Acronyms + Dynamic Descriptions)

```yaml
- name: STATUS
  value-mappings:
    # Static: Common shortcuts
    mappings:
      - user-term: active
        database-value: ACTIVE
        aliases: [open, enabled, current]
      
      - user-term: closed
        database-value: CLOSED
        aliases: [inactive, disabled]
    
    # Dynamic: All statuses with descriptions
    sources:
      - type: sql
        name: status_with_descriptions
        enabled: true
        sql: |
          SELECT
            status_code AS "ID",
            status_code AS "VALUE",
            status_code || ' - ' || description AS "TEXT"
          FROM client_status_codes
```

---

## Configuration

### Enable Both Static and Dynamic

```yaml
mill:
  metadata:
    annotations: file
    file:
      repository:
        path: classpath:metadata/complete.yml
  
  services:
    ai-nl2data:
      enable: true  # Enables ValueMappingComponents
```

### Disable Dynamic Sources (Testing)

```yaml
# In metadata YAML, set enabled: false
sources:
  - type: sql
    name: my_source
    enabled: false  # ← Disable
    sql: ...
```

---

## API Reference

### MetadataProvider

```java
// Get all static value mappings
Collection<ValueMappingWithContext> getAllValueMappings();

// Get all SQL sources
Collection<ValueMappingSourceWithContext> getAllValueMappingSources();
```

### ValueMappingSourceWithContext

```java
record ValueMappingSourceWithContext(
    String schemaName,
    String tableName,
    String attributeName,
    String sourceName,
    String sql,
    String description,
    boolean enabled
) {
    String getFullyQualifiedName();  // Returns: "SCHEMA.TABLE.ATTRIBUTE"
}
```

---

## Implementation Details

### ValueMappingComponents Flow

```java
@EventListener(ApplicationReadyEvent.class)
public void onApplicationReady() {
    // 1. Ingest static mappings
    ingestStaticValueMappings();
    
    // 2. Ingest dynamic mappings from SQL
    ingestDynamicValueMappings();
}

private void ingestDynamicValueMappings() {
    // Get all SQL sources
    val allSources = metadataProvider.getAllValueMappingSources();
    
    for (var source : allSources) {
        if (!source.enabled()) {
            continue;
        }
        
        // Execute SQL
        val request = QueryRequest.newBuilder()
            .setStatement(SQLStatement.newBuilder()
                .setSql(source.sql())
                .build())
            .build();
        
        val result = dataDispatcher.execute(request);
        val recordReader = RecordReaders.recordReader(result);
        
        // Create documents from results
        while (recordReader.next()) {
            val id = recordReader.getString(0);
            val value = recordReader.getString(1);
            val text = recordReader.getString(2);
            
            val doc = new ValueDocument(id, targetId, value, text);
            docs.add(doc);
        }
        
        // Ingest
        repository.ingest(docs);
    }
}
```

---

## Benefits

### Static Mappings
- ✅ Version controlled
- ✅ Curated by data team
- ✅ Multi-language support
- ✅ Rich aliases
- ✅ Fast (no SQL execution)

### Dynamic SQL Sources
- ✅ Always up-to-date
- ✅ Discovers new values automatically
- ✅ Can use reference tables
- ✅ Computed display text
- ✅ No manual maintenance

### Combined Approach
- ✅ **Best of both worlds**
- ✅ Manual for important terms (USA, UK)
- ✅ Automatic for discovery (all countries)
- ✅ Flexible per attribute

---

## Example: Country Mapping

### Static Part (metadata YAML)
```yaml
mappings:
  - user-term: USA
    database-value: US
    aliases: [America, United States]
```

**Creates RAG Document:**
```
ID: MONETA-CLIENTS-COUNTRY-usa-en
Value: US
Text: "USA America United States COUNTRY CLIENTS"
```

### Dynamic Part (metadata YAML)
```yaml
sources:
  - type: sql
    name: all_countries
    sql: |
      SELECT "COUNTRY" AS "ID", "COUNTRY" AS "VALUE", "COUNTRY" AS "TEXT"
      FROM "CLIENTS" WHERE "COUNTRY" IS NOT NULL
```

**Executes SQL, creates documents:**
```
ID: MONETA-CLIENTS-COUNTRY-US
Value: US
Text: "US"

ID: MONETA-CLIENTS-COUNTRY-CA
Value: CA
Text: "CA"

ID: MONETA-CLIENTS-COUNTRY-MX
Value: MX
Text: "MX"
...
```

### Combined Result

RAG vector store contains:
- Static: "USA", "America", "United States" → US (with rich aliases)
- Dynamic: All actual country codes from database → themselves

**Query:** "show me customers in America"  
**Match:** Static mapping "America" → US  
**SQL:** `WHERE country = 'US'` ✅

**Query:** "customers in Canada"  
**Match:** Dynamic mapping "CA" → CA (or "Canada" if in ref table)  
**SQL:** `WHERE country = 'CA'` ✅

---

## When to Use Each

| Scenario | Use Static | Use SQL | Use Both |
|----------|-----------|---------|----------|
| **Acronyms** (USA, UK) | ✅ | | ✅ |
| **Synonyms** (gold=premium) | ✅ | | |
| **Multi-language** | ✅ | | ✅ |
| **Many values** (100s of countries) | | ✅ | ✅ |
| **Reference tables** (with names) | | ✅ | |
| **Changing data** (new values added) | | ✅ | ✅ |
| **Computed text** (concat columns) | | ✅ | |
| **Common + All** (USA + all countries) | | | ✅ |

---

## SQL Query Patterns

### Pattern 1: Simple Distinct

```sql
SELECT DISTINCT
  "COLUMN" AS "ID",
  "COLUMN" AS "VALUE",
  "COLUMN" AS "TEXT"
FROM "SCHEMA"."TABLE"
WHERE "COLUMN" IS NOT NULL
```

**Use for:** Simple enums, status codes, categories

### Pattern 2: Reference Table with Names

```sql
SELECT
  code AS "ID",
  code AS "VALUE",
  name AS "TEXT"
FROM reference.lookup_table
ORDER BY name
```

**Use for:** Country codes, currency codes, product codes

### Pattern 3: Rich Descriptions

```sql
SELECT
  code AS "ID",
  code AS "VALUE",
  code || ' - ' || name || ' (' || category || ')' AS "TEXT"
FROM lookup_table
```

**Use for:** Better semantic search with context

### Pattern 4: Multiple Tables Join

```sql
SELECT
  p.product_code AS "ID",
  p.product_code AS "VALUE",
  p.product_name || ' by ' || v.vendor_name AS "TEXT"
FROM products p
JOIN vendors v ON p.vendor_id = v.id
WHERE p.active = TRUE
```

**Use for:** Complex lookups with related data

---

## Configuration

### Metadata YAML Structure

```yaml
value-mappings:
  # Static mappings (manual)
  mappings:
    - user-term: string
      database-value: string
      display-value: string (optional)
      description: string (optional)
      language: string (default: "en")
      aliases: [string, ...] (optional)
  
  # Dynamic sources (SQL)
  sources:
    - type: sql
      name: string (required)
      description: string (optional)
      enabled: boolean (default: true)
      sql: string (required - must return ID, VALUE, TEXT)
      cache-ttl-seconds: integer (default: 3600)
      cron: string (optional, for future scheduling)
```

---

## Troubleshooting

### SQL Source Not Working

**Check logs:**
```
INFO  Processing SQL source: my_source for SCHEMA.TABLE.ATTR
ERROR Error processing SQL source my_source: [error message]
```

**Common issues:**
1. SQL syntax error
2. Wrong number of columns (must be exactly 3)
3. Column names not "ID", "VALUE", "TEXT"
4. NULL values not filtered

**Fix:**
```sql
-- ❌ Wrong - missing TEXT column
SELECT "COUNTRY", "COUNTRY" FROM clients

-- ✅ Correct - all 3 columns
SELECT 
  "COUNTRY" AS "ID",
  "COUNTRY" AS "VALUE",
  "COUNTRY" AS "TEXT"
FROM clients
WHERE "COUNTRY" IS NOT NULL
```

### No Dynamic Mappings Ingested

**Check:**
1. Is `enabled: true`?
2. Is service `ai-nl2data` enabled?
3. Check logs for "No enabled SQL sources found"

### Duplicate Documents

**Issue:** Same value in both static and SQL

**Solution:** Use one or the other, or use SQL for discovery and static for aliases:

```yaml
# Good approach
mappings:
  - user-term: USA        # Alias only
    database-value: US
    aliases: [America, United States]

sources:
  - sql: SELECT DISTINCT country...  # Actual values
```

---

## Performance

### Startup Time

- **Static:** Instant (already in memory)
- **SQL Sources:** Depends on query
  - Simple DISTINCT: ~100ms for 1000 rows
  - Complex JOIN: ~500ms+
  - Multiple sources: Sequential execution

### Recommendations

1. **Limit SQL complexity** - Use simple queries
2. **Filter data** - WHERE clauses to reduce rows
3. **Index columns** - Ensure DISTINCT uses indexes
4. **Cache TTL** - Set appropriate cache duration
5. **Disable in dev** - Set `enabled: false` for large sources during development

---

## Example Complete File

See: `test/datasets/moneta/metadata-complete-with-sql-sources.yml`

This example includes:
- **SEGMENT** - Static only (fixed segments)
- **COUNTRY** - Static (acronyms) + Dynamic (all countries)
- **STATUS** - Dynamic only (from database)
- **ACCOUNT_TYPE** - Static only (fixed types)
- **CURRENCY** - Static (major currencies) + Dynamic (all used currencies)

---

## Summary

✅ **Static mappings** for curated values, acronyms, multi-language  
✅ **SQL sources** for discovery, reference tables, dynamic values  
✅ **Both in same file** - easy to configure  
✅ **Automatic ingestion** - on startup  
✅ **Flexible** - use what you need per attribute  

**To use:**
1. Add `value-mappings` to your metadata YAML
2. Choose `mappings:` (static), `sources:` (SQL), or both
3. Restart application
4. Value mappings automatically ingested into RAG!

---

**File:** `ai/mill-ai-core/.../ValueMappingComponents.java`  
**Status:** ✅ Supports both static and dynamic sources  
**Ready to use!** 🚀

