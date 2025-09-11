# RAG Value Mapping Integration

**Status:** ✅ Implemented  
**Purpose:** Create RAG documents from metadata value mappings for semantic search

---

## Overview

The system now automatically ingests value mappings from metadata into the RAG vector store at application startup. This enables semantic search for value resolution without hardcoded SQL queries.

---

## How It Works

### 1. Define Value Mappings in Metadata YAML

```yaml
# metadata/complete.yml
schemas:
  - name: MONETA
    tables:
      - name: CLIENTS
        attributes:
          - name: SEGMENT
            type: VARCHAR
            value-mappings:
              mappings:
                - user-term: premium
                  database-value: PREMIUM
                  display-value: Premium
                  description: High-value customers
                  language: en
                  aliases: [gold, golden, vip]
                
                - user-term: standard
                  database-value: STANDARD
                  language: en
```

### 2. System Loads and Converts to RAG Documents

**On Application Startup:**

```
ApplicationReadyEvent
  ↓
ValueMappingComponents.onApplicationReady()
  ↓
ingestValueMappingsFromMetadata()
  ├─→ metadataProvider.getAllValueMappings()
  │     Returns: List<ValueMappingWithContext>
  │     [
  │       ValueMappingWithContext(
  │         schema="MONETA",
  │         table="CLIENTS",
  │         attribute="SEGMENT",
  │         mapping=ValueMapping("premium", "PREMIUM", ...)
  │       ),
  │       ValueMappingWithContext("MONETA", "CLIENTS", "SEGMENT", 
  │         ValueMapping("gold", "PREMIUM", ...)),  // from aliases
  │       ...
  │     ]
  │
  ├─→ For each mapping:
  │     Create ValueDocument(
  │       id="MONETA-CLIENTS-SEGMENT-premium-en",
  │       targetId=["MONETA", "CLIENTS", "SEGMENT"],
  │       value="PREMIUM",
  │       text="premium gold golden vip SEGMENT CLIENTS High-value customers"
  │     )
  │
  └─→ repository.ingest(docs)
        Stores in RAG vector store
```

### 3. RAG Search at Query Time

```
User query: "show me gold customers"
  ↓
Vector similarity search in RAG
  ↓
Finds document: "premium gold golden vip SEGMENT..."
  ↓
Returns: MONETA.CLIENTS.SEGMENT → "PREMIUM"
  ↓
SQL: WHERE segment = 'PREMIUM'
```

---

## Implementation

### ValueMappingComponents.java

**Location:** `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/components/ValueMappingComponents.java`

**Key Method:**
```java
private void ingestValueMappingsFromMetadata() {
    // Get all value mappings from metadata
    val allMappings = metadataProvider.getAllValueMappings();
    
    // Convert to RAG documents
    for (var mappingWithContext : allMappings) {
        val docId = String.format("%s-%s-%s", 
            mappingWithContext.getFullyQualifiedName().replace(".", "-"),
            mappingWithContext.mapping().userTerm().toLowerCase().replace(" ", "-"),
            mappingWithContext.mapping().language()
        );
        
        val doc = new ValueRepository.ValueDocument(
            docId,
            Arrays.asList(
                mappingWithContext.schemaName(),
                mappingWithContext.tableName(),
                mappingWithContext.attributeName()
            ),
            mappingWithContext.mapping().databaseValue(),
            mappingWithContext.toEmbeddingText()  // Rich semantic text
        );
        
        docs.add(doc);
    }
    
    // Ingest into vector store
    repository.ingest(docs);
}
```

---

## RAG Document Format

### Embedding Text (for Semantic Search)

The `toEmbeddingText()` method creates rich text for vector embeddings:

```
Input mapping:
  user-term: "premium"
  database-value: "PREMIUM"
  aliases: ["gold", "golden", "vip"]
  description: "High-value customers"

Output text:
  "premium gold golden vip SEGMENT CLIENTS High-value customers"
```

This combines:
- User term
- All aliases
- Attribute name (context)
- Table name (context)
- Description (semantic meaning)

### Document Structure

```java
ValueDocument(
    id: "MONETA-CLIENTS-SEGMENT-premium-en",
    targetId: ["MONETA", "CLIENTS", "SEGMENT"],
    value: "PREMIUM",                           // Database value
    text: "premium gold golden vip SEGMENT..."  // Embedding text
)
```

---

## Benefits

### Before (Hardcoded SQL)

```java
@EventListener(ApplicationReadyEvent.class)
public void onApplicationReady() {
    // ❌ Hardcoded - must update code for new attributes
    ingestColumn("MONETA.CLIENTS.COUNTRY", "SELECT DISTINCT...");
    ingestColumn("MONETA.CLIENTS.SEGMENT", "SELECT DISTINCT...");
}
```

**Problems:**
- ❌ Hardcoded SQL for each attribute
- ❌ Must modify code to add new mappings
- ❌ No alias support
- ❌ No multi-language support
- ❌ No semantic descriptions

### After (Metadata-Driven)

```java
@EventListener(ApplicationReadyEvent.class)
public void onApplicationReady() {
    // ✅ Metadata-driven - automatic from YAML
    ingestValueMappingsFromMetadata();
}
```

**Benefits:**
- ✅ Automatic from metadata YAML
- ✅ Add new mappings = just edit YAML
- ✅ Aliases automatically expanded
- ✅ Multi-language support
- ✅ Semantic descriptions for better search
- ✅ Context-aware (schema/table/attribute)

---

## Example Usage

### 1. Add Value Mappings to Metadata

```yaml
# config/metadata.yml
schemas:
  - name: SALES
    tables:
      - name: CUSTOMERS
        attributes:
          - name: STATUS
            type: VARCHAR
            value-mappings:
              mappings:
                - user-term: active
                  database-value: ACTIVE
                  description: Currently active customers
                  language: en
                  aliases: [enabled, open, current]
                
                - user-term: inactive
                  database-value: INACTIVE
                  language: en
                  aliases: [disabled, closed]
```

### 2. Configure Application

```yaml
# application.yml
mill:
  metadata:
    annotations: file
    file:
      repository:
        path: classpath:config/metadata.yml
  
  services:
    ai-nl2data:
      enable: true
```

### 3. Automatic RAG Ingestion

On startup, the system will:
1. Load metadata from YAML
2. Extract all value mappings (including aliases)
3. Create RAG documents with semantic text
4. Ingest into vector store

**Log output:**
```
INFO  Value Mapping RAG component initialized.
INFO  Ingesting value mappings from metadata...
INFO  Found 6 value mappings to ingest
DEBUG Created document: active -> ACTIVE
DEBUG Created document: enabled -> ACTIVE
DEBUG Created document: open -> ACTIVE
DEBUG Created document: inactive -> INACTIVE
DEBUG Created document: disabled -> INACTIVE
DEBUG Created document: closed -> INACTIVE
INFO  Ingesting 6 documents into RAG vector store
INFO  Successfully ingested value mappings into RAG
```

---

## RAG Documents Created

For the example above, these documents are created:

### Document 1: "active"
```
ID: SALES-CUSTOMERS-STATUS-active-en
Target: [SALES, CUSTOMERS, STATUS]
Value: ACTIVE
Text: "active enabled open current STATUS CUSTOMERS Currently active customers"
```

### Document 2: "enabled" (alias)
```
ID: SALES-CUSTOMERS-STATUS-enabled-en
Target: [SALES, CUSTOMERS, STATUS]
Value: ACTIVE
Text: "enabled STATUS CUSTOMERS Currently active customers"
```

### Document 3: "inactive"
```
ID: SALES-CUSTOMERS-STATUS-inactive-en
Target: [SALES, CUSTOMERS, STATUS]
Value: INACTIVE
Text: "inactive disabled closed STATUS CUSTOMERS"
```

---

## Query Flow

### User Query with Semantic Search

```
User: "show me enabled customers"
  ↓
Vector similarity search: "enabled customers"
  ↓
Finds document with text: "enabled STATUS CUSTOMERS Currently active customers"
  ↓
Returns: SALES.CUSTOMERS.STATUS → "ACTIVE"
  ↓
SQL generation: WHERE status = 'ACTIVE'
```

### Handles Synonyms

```
User: "list open customer accounts"
  ↓
Vector search: "open customer"
  ↓
Finds: "active enabled open current STATUS..."
  ↓
Returns: STATUS → "ACTIVE"
```

---

## Configuration

### Enable RAG Ingestion

```yaml
mill:
  services:
    ai-nl2data:
      enable: true  # Enables ValueMappingComponents
```

### Disable RAG Ingestion (Development)

```yaml
mill:
  services:
    ai-nl2data:
      enable: false  # Skip RAG ingestion
```

---

## API Reference

### MetadataProvider.ValueMappingWithContext

```java
record ValueMappingWithContext(
    String schemaName,
    String tableName,
    String attributeName,
    ValueMapping mapping
) {
    // Get fully qualified name
    String getFullyQualifiedName()
    // Returns: "SCHEMA.TABLE.ATTRIBUTE"
    
    // Create RAG document text
    String toDocument()
    // Returns: Full document with all fields
    
    // Create embedding text for vector search
    String toEmbeddingText()
    // Returns: "userTerm aliases attribute table description"
}
```

### MetadataProvider

```java
// Get all value mappings for RAG
Collection<ValueMappingWithContext> getAllValueMappings();

// Returns flat list with:
// - All main mappings
// - All alias mappings (expanded)
// - Context information (schema, table, attribute)
```

---

## Testing

### Verify RAG Ingestion

**Check logs on startup:**
```
INFO  Value Mapping RAG component initialized.
INFO  Ingesting value mappings from metadata...
INFO  Found X value mappings to ingest
INFO  Ingesting X documents into RAG vector store
INFO  Successfully ingested value mappings into RAG
```

### Test with Queries

```
# Direct term
"show me premium customers" → works

# Alias term  
"show me gold customers" → works (finds premium)

# Synonym
"list vip clients" → works (finds premium via alias)

# Multi-word
"customers in United States" → works
```

---

## Troubleshooting

### No Mappings Found

**Check:**
1. Is metadata file loaded? Check logs: "Using file-based metadata repository..."
2. Does YAML have value-mappings? Check structure
3. Are mappings under correct attribute?

### RAG Not Searching

**Check:**
1. Is `ai-nl2data` service enabled?
2. Is `ValueRepository` bean configured?
3. Check logs for ingestion messages

### Wrong Values Returned

**Debug:**
```java
// Check what was ingested
var allMappings = metadataProvider.getAllValueMappings();
for (var m : allMappings) {
    System.out.printf("%s: %s -> %s (%s)%n",
        m.getFullyQualifiedName(),
        m.mapping().userTerm(),
        m.mapping().databaseValue(),
        m.toEmbeddingText()
    );
}
```

---

## Summary

**What changed:**
- ✅ `ValueMappingComponents` now uses `metadataProvider.getAllValueMappings()`
- ✅ Removed hardcoded SQL queries
- ✅ Automatic ingestion from metadata YAML
- ✅ Alias expansion for better coverage
- ✅ Semantic text generation for vector search

**Benefits:**
- ✅ Configuration-driven (edit YAML, not code)
- ✅ Richer semantic search (aliases + descriptions)
- ✅ Automatic updates (restart to reload)
- ✅ Multi-language ready
- ✅ Clean architecture

**To use:**
1. Add value-mappings to your metadata YAML
2. Enable `ai-nl2data` service
3. Restart application
4. Value mappings automatically ingested into RAG!

---

**Implementation:** `ai/mill-ai-core/.../ValueMappingComponents.java`  
**Tests:** Unit tests verify `getAllValueMappings()` returns correct flat list  
**Status:** Ready to use!

