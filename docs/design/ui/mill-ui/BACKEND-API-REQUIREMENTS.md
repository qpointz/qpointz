# Backend API Requirements

This document specifies the REST API and streaming endpoints that the backend must provide to replace the current frontend mock data layer.

---

## Table of Contents

1. [Overview](#overview)
2. [General Conventions](#general-conventions)
3. [Domain: Schema (Data Model)](#domain-schema-data-model)
4. [Domain: Concepts (Knowledge)](#domain-concepts-knowledge)
5. [Domain: Queries (Analysis)](#domain-queries-analysis)
6. [Domain: Chat (Conversations)](#domain-chat-conversations)
7. [Domain: Inline Chat (Context-Aware)](#domain-inline-chat-context-aware)
8. [Domain: Overview (Dashboard)](#domain-overview-dashboard)
9. [Domain: Global Search](#domain-global-search)
10. [Data Models Reference](#data-models-reference)
11. [Streaming Protocol](#streaming-protocol)
12. [Error Handling](#error-handling)

---

## Overview

The frontend currently uses three data layers that the backend must replace:

| Layer | Current Source | What Backend Provides |
|-------|---------------|----------------------|
| **Static data** | `src/data/mock*.ts` (in-memory arrays) | REST endpoints returning lists, trees, and details |
| **Async services** | `src/services/mockApi.ts` (simulated delays) | REST + streaming endpoints for queries and chat |
| **State persistence** | `localStorage` (chat conversations) | Server-side persistence for conversations |

The frontend will consume the backend through `src/services/api.ts`, which is already structured as a swap point for real implementations.

---

## General Conventions

- **Base URL**: Configurable, e.g. `GET {BASE_URL}/api/v1/...`
- **Content-Type**: `application/json` for request/response bodies
- **Streaming**: `text/event-stream` (Server-Sent Events) for chat responses
- **Authentication**: Bearer token in `Authorization` header (specifics TBD)
- **Pagination**: Optional, via `?offset=0&limit=50` query parameters where applicable
- **Timestamps**: Unix epoch milliseconds (number)

---

## Domain: Schema (Data Model)

The Model view displays a hierarchical schema tree (schemas > tables > attributes) and entity detail panels with facets.

### GET /api/v1/schema/tree

Returns the full schema tree hierarchy.

**Response:**
```json
{
  "tree": [
    {
      "id": "sales",
      "type": "SCHEMA",
      "name": "sales",
      "children": [
        {
          "id": "sales.customers",
          "type": "TABLE",
          "name": "customers",
          "children": [
            {
              "id": "sales.customers.customer_id",
              "type": "ATTRIBUTE",
              "name": "customer_id",
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

**Frontend usage**: Renders the sidebar schema tree. Tree is loaded once on mount.

### GET /api/v1/schema/entities/{entityId}

Returns a single entity by its dot-separated ID (e.g. `sales.customers.customer_id`).

**Response:**
```json
{
  "id": "sales.customers.customer_id",
  "type": "ATTRIBUTE",
  "name": "customer_id"
}
```

**Frontend usage**: Resolves an entity from URL parameters for header context and inline chat.

### GET /api/v1/schema/entities/{entityId}/facets

Returns descriptive, structural, and relation facets for a given entity.

**Response:**
```json
{
  "descriptive": {
    "displayName": "Customer ID",
    "description": "Unique identifier for each customer",
    "businessMeaning": "Primary key used across all customer-related tables",
    "businessDomain": "Customer Management",
    "businessOwner": "Data Engineering",
    "tags": ["identifier", "primary-key", "customer"],
    "synonyms": ["cust_id", "client_id"]
  },
  "structural": {
    "physicalName": "customer_id",
    "physicalType": "INTEGER",
    "precision": null,
    "scale": null,
    "isPrimaryKey": true,
    "isForeignKey": false,
    "isUnique": true,
    "nullable": false,
    "defaultValue": "AUTO_INCREMENT"
  },
  "relations": [
    {
      "id": "rel-1",
      "name": "customer_orders",
      "sourceEntity": "sales.customers",
      "targetEntity": "sales.orders",
      "cardinality": "1:N",
      "relationType": "FOREIGN_KEY",
      "description": "Each customer can have many orders"
    }
  ]
}
```

**Frontend usage**: Displayed in the `EntityDetails` component when a tree node is selected.

---

## Domain: Concepts (Knowledge)

The Knowledge view displays business concepts organized by categories and tags, with filtering.

### GET /api/v1/concepts

Returns all concepts. Supports optional filtering.

**Query parameters:**
- `category` (optional): Filter by category name
- `tag` (optional): Filter by tag name

**Response:**
```json
{
  "concepts": [
    {
      "id": "customer-lifetime-value",
      "name": "Customer Lifetime Value (CLV)",
      "category": "Analytics",
      "tags": ["revenue", "customer", "metric"],
      "description": "The total revenue a business can expect from a single customer...",
      "sql": "SELECT customer_id, SUM(total_amount) as lifetime_value FROM sales.orders GROUP BY customer_id",
      "relatedEntities": ["sales.customers", "sales.orders"],
      "source": "MANUAL",
      "createdAt": 1700000000000,
      "updatedAt": 1700100000000
    }
  ]
}
```

**Frontend usage**: Renders the concepts list in the sidebar. Filtering is triggered when the user clicks a category or tag.

### GET /api/v1/concepts/{conceptId}

Returns a single concept by ID.

**Response:** Same shape as a single item from the array above.

**Frontend usage**: Resolves a concept from URL parameters; displays `ConceptDetails` panel.

### GET /api/v1/concepts/categories

Returns all categories with their concept counts.

**Response:**
```json
{
  "categories": [
    { "name": "Analytics", "count": 4 },
    { "name": "Operations", "count": 3 },
    { "name": "Finance", "count": 2 },
    { "name": "Customer Intelligence", "count": 1 }
  ]
}
```

**Frontend usage**: Renders the category filter list in the sidebar.

### GET /api/v1/concepts/tags

Returns all tags with their concept counts.

**Response:**
```json
{
  "tags": [
    { "name": "revenue", "count": 5 },
    { "name": "customer", "count": 4 },
    { "name": "metric", "count": 3 }
  ]
}
```

**Frontend usage**: Renders the tag filter list in the sidebar.

---

## Domain: Queries (Analysis)

The Analysis view lists saved queries and provides a SQL editor with execution.

### GET /api/v1/queries

Returns all saved queries.

**Response:**
```json
{
  "queries": [
    {
      "id": "top-customers",
      "name": "Top Customers by Revenue",
      "description": "Find the highest-spending customers",
      "sql": "SELECT c.customer_name, SUM(o.total_amount) AS total_spent...",
      "createdAt": 1700000000000,
      "updatedAt": 1700100000000,
      "tags": ["customers", "revenue"]
    }
  ]
}
```

**Frontend usage**: Renders the query list in the sidebar.

### GET /api/v1/queries/{queryId}

Returns a single saved query by ID.

**Response:** Same shape as a single item from the array above.

**Frontend usage**: Loads a query from URL parameters into the SQL editor.

### POST /api/v1/queries/execute

Executes a SQL query and returns results.

**Request:**
```json
{
  "sql": "SELECT c.customer_name, SUM(o.total_amount) AS total_spent FROM sales.customers c JOIN sales.orders o ON c.customer_id = o.customer_id GROUP BY c.customer_name ORDER BY total_spent DESC LIMIT 10"
}
```

**Response:**
```json
{
  "columns": [
    { "name": "customer_name", "type": "VARCHAR" },
    { "name": "total_spent", "type": "DECIMAL" }
  ],
  "rows": [
    { "customer_name": "Acme Corp", "total_spent": 125430.50 },
    { "customer_name": "TechStart Inc", "total_spent": 98200.00 }
  ],
  "rowCount": 10,
  "executionTimeMs": 342
}
```

**Error response (SQL errors):**
```json
{
  "error": "Syntax error near \"SELCT\": expected SELECT, INSERT, UPDATE, or DELETE",
  "code": "SQL_SYNTAX_ERROR"
}
```

**Frontend usage**: Called when the user clicks "Execute" in the query editor. Results displayed in the results table. Errors shown as error messages.

---

## Domain: Chat (Conversations)

The Chat view supports multiple conversations with streaming assistant responses.

### GET /api/v1/conversations

Returns all conversations for the current user.

**Response:**
```json
{
  "conversations": [
    {
      "id": "conv-1",
      "title": "Data modeling help",
      "createdAt": 1700000000000,
      "updatedAt": 1700100000000,
      "messages": [
        {
          "id": "msg-1",
          "conversationId": "conv-1",
          "role": "user",
          "content": "How do I create a star schema?",
          "timestamp": 1700000000000
        },
        {
          "id": "msg-2",
          "conversationId": "conv-1",
          "role": "assistant",
          "content": "A star schema consists of...",
          "timestamp": 1700000001000
        }
      ]
    }
  ]
}
```

**Frontend usage**: Loaded on mount to populate the sidebar conversation list. Currently stored in localStorage.

### POST /api/v1/conversations

Creates a new conversation.

**Request:**
```json
{
  "title": "New conversation"
}
```

**Response:**
```json
{
  "id": "conv-new",
  "title": "New conversation",
  "createdAt": 1700000000000,
  "updatedAt": 1700000000000,
  "messages": []
}
```

### DELETE /api/v1/conversations/{conversationId}

Deletes a conversation and all its messages.

### POST /api/v1/conversations/{conversationId}/messages (Streaming)

Sends a user message and streams the assistant's response.

**Request:**
```json
{
  "content": "How do I create a star schema?"
}
```

**Response**: Server-Sent Events stream (see [Streaming Protocol](#streaming-protocol)).

Each event contains a text chunk. The frontend accumulates chunks to build the full assistant response incrementally.

**Frontend usage**: Called by `ChatContext.sendMessage()`. The response is streamed word-by-word and rendered progressively in the chat UI.

---

## Domain: Inline Chat (Context-Aware)

Context-aware chat sessions attached to specific schema entities, concepts, or queries. These are displayed in a right-side drawer.

### POST /api/v1/inline-chat/messages (Streaming)

Sends a message within a context-aware inline chat session.

**Request:**
```json
{
  "contextType": "model",
  "contextId": "sales.customers.customer_id",
  "message": "What is the distribution of this column?"
}
```

Where:
- `contextType`: One of `"model"`, `"knowledge"`, `"analysis"`
- `contextId`: The entity ID, concept ID, or query ID that provides context
- `message`: The user's message

**Response**: Server-Sent Events stream (see [Streaming Protocol](#streaming-protocol)).

The backend should use the context to provide relevant, domain-specific responses:
- **model**: Responses about schema structure, data quality, relationships, column metadata
- **knowledge**: Responses about business concept definitions, related concepts, SQL refinements
- **analysis**: Responses about query optimization, result interpretation, follow-up queries

**Frontend usage**: Called by `InlineChatContext.sendMessage()`. Sessions are managed client-side; the backend is stateless per request (context is passed each time).

---

## Domain: Overview (Dashboard)

The Overview dashboard displays summary statistics.

### GET /api/v1/stats

Returns aggregate counts for the dashboard.

**Response:**
```json
{
  "schemaCount": 3,
  "tableCount": 12,
  "conceptCount": 10,
  "queryCount": 6
}
```

**Frontend usage**: Rendered as stat cards on the overview page. Currently computed from mock array lengths.

---

## Domain: Global Search

The header contains a global search field (gated by `headerGlobalSearch` feature flag) that searches across all object types — views, schema entities, concepts, and saved queries.

### GET /api/v1/search

Returns search results across all domains. Results are grouped by type and capped at 20.

**Query parameters:**
- `q` (required): Search query string. Queries shorter than 2 characters return empty results.

**Response:**
```json
{
  "results": [
    {
      "id": "sales.customers",
      "name": "Customers",
      "type": "table",
      "description": "Customer master data",
      "breadcrumb": "sales",
      "route": "/model/sales/customers"
    },
    {
      "id": "customer-lifetime-value",
      "name": "Customer Lifetime Value (CLV)",
      "type": "concept",
      "description": "The total revenue a business can expect from a single customer...",
      "breadcrumb": "Analytics",
      "route": "/knowledge/customer-lifetime-value"
    }
  ]
}
```

**Search behavior:**
- Case-insensitive substring matching across name, description, tags, and synonyms
- Results grouped by `type`: `view`, `schema`, `table`, `attribute`, `concept`, `query`
- Maximum 20 results returned
- Each result includes a `route` for frontend navigation

**Frontend usage:** The `GlobalSearch` component debounces input (200ms) and displays results in a floating dropdown. If no results match, an "Ask in Chat" button offers to create a new general chat conversation with the search query as the first message. Keyboard navigation (arrow keys + Enter) and `Ctrl+K` / `Cmd+K` shortcut to open.

---

## Data Models Reference

### SchemaEntity
```typescript
{
  id: string;              // Dot-separated: "schema.table.attribute"
  type: "SCHEMA" | "TABLE" | "ATTRIBUTE";
  name: string;
  children?: SchemaEntity[];
}
```

### EntityFacets
```typescript
{
  descriptive?: {
    displayName?: string;
    description?: string;
    businessMeaning?: string;
    businessDomain?: string;
    businessOwner?: string;
    tags?: string[];
    synonyms?: string[];
  };
  structural?: {
    physicalName?: string;
    physicalType?: string;
    precision?: number;
    scale?: number;
    isPrimaryKey?: boolean;
    isForeignKey?: boolean;
    isUnique?: boolean;
    nullable?: boolean;
    defaultValue?: string;
  };
  relations?: Array<{
    id: string;
    name: string;
    sourceEntity: string;
    targetEntity: string;
    cardinality: "1:1" | "1:N" | "N:1" | "N:N";
    relationType: "FOREIGN_KEY" | "LOGICAL" | "HIERARCHICAL";
    description?: string;
  }>;
}
```

### Concept
```typescript
{
  id: string;
  name: string;
  category: string;
  tags: string[];
  description: string;
  sql?: string;
  relatedEntities?: string[];
  source?: "MANUAL" | "INFERRED" | "IMPORTED";
  createdAt?: number;    // epoch ms
  updatedAt?: number;    // epoch ms
}
```

### SavedQuery
```typescript
{
  id: string;
  name: string;
  description?: string;
  sql: string;
  createdAt: number;     // epoch ms
  updatedAt: number;     // epoch ms
  tags?: string[];
}
```

### QueryResult
```typescript
{
  columns: Array<{ name: string; type: string }>;
  rows: Array<Record<string, string | number | boolean | null>>;
  rowCount: number;
  executionTimeMs: number;
}
```

### SearchResult
```typescript
{
  id: string;
  name: string;
  type: "view" | "schema" | "table" | "attribute" | "concept" | "query";
  description?: string;
  breadcrumb?: string;   // e.g. "sales > customers" for attributes, category for concepts
  route: string;         // frontend route for navigation, e.g. "/model/sales/customers"
}
```

### Message
```typescript
{
  id: string;
  conversationId: string;
  role: "user" | "assistant";
  content: string;       // May contain Markdown, code blocks, tables
  timestamp: number;     // epoch ms
}
```

### Conversation
```typescript
{
  id: string;
  title: string;
  createdAt: number;     // epoch ms
  updatedAt: number;     // epoch ms
  messages: Message[];
}
```

---

## Streaming Protocol

Chat and inline chat responses use **Server-Sent Events (SSE)** for streaming.

**Content-Type**: `text/event-stream`

**Event format:**
```
data: {"chunk": "Here is "}

data: {"chunk": "the first "}

data: {"chunk": "part of the response."}

data: [DONE]
```

Each `data` line contains a JSON object with a `chunk` field (a text fragment). The frontend concatenates chunks to build the full response. The stream ends with `data: [DONE]`.

**Alternative**: If the backend prefers, it can use a simpler newline-delimited format or WebSocket. The key requirement is that text arrives incrementally so the UI can render progressively (word-by-word or sentence-by-sentence).

**Response content**: The assistant's content may include Markdown formatting:
- **Bold**, *italic*, headings
- Code blocks with language tags (```sql, ```typescript, etc.)
- Markdown tables
- Numbered and bulleted lists

The frontend already renders Markdown, so the backend should return Markdown-formatted text.

---

## Error Handling

All endpoints should return errors in a consistent format:

```json
{
  "error": "Human-readable error message",
  "code": "ERROR_CODE"
}
```

**HTTP status codes:**
- `400` -- Bad request (invalid parameters, malformed SQL)
- `401` -- Unauthorized
- `404` -- Resource not found (entity, concept, query, conversation)
- `500` -- Internal server error

---

## 17. Feature Flags

### GET /api/v1/features

Returns feature flags for the current user/session. The backend only needs to include flags it wants to override. Omitted flags default to `true` on the frontend.

**Response `200 OK`:**

```json
{
  "viewOverview": true,
  "viewModel": true,
  "viewKnowledge": true,
  "viewAnalysis": true,
  "viewChat": true,
  "inlineChatEnabled": true,
  "inlineChatModelContext": true,
  "inlineChatModelSchema": true,
  "inlineChatModelTable": true,
  "inlineChatModelColumn": true,
  "inlineChatKnowledgeContext": true,
  "inlineChatAnalysisContext": true,
  "inlineChatMultiSession": true,
  "inlineChatSessionGrouping": true,
  "inlineChatGreeting": true,
  "modelDescriptiveFacet": true,
  "modelStructuralFacet": true,
  "modelRelationsFacet": true,
  "modelQuickBadges": true,
  "modelPhysicalType": true,
  "knowledgeDescription": true,
  "knowledgeTags": true,
  "knowledgeSqlDefinition": true,
  "knowledgeRelatedEntities": true,
  "knowledgeMetadata": true,
  "knowledgeSourceBadge": true,
  "analysisFormatSql": true,
  "analysisCopySql": true,
  "analysisClearSql": true,
  "analysisExecuteQuery": true,
  "analysisQueryResults": true,
  "sidebarCollapsible": true,
  "sidebarKnowledgeCategories": true,
  "sidebarKnowledgeTags": true,
  "sidebarAnalysisBadge": true,
  "relatedContentEnabled": true,
  "relatedContentModelContext": true,
  "relatedContentModelSchema": true,
  "relatedContentModelTable": true,
  "relatedContentModelColumn": true,
  "relatedContentKnowledgeContext": true,
  "relatedContentAnalysisContext": true,
  "relatedContentInDrawer": true,
  "chatAttachButton": true,
  "chatDictateButton": true,
  "headerGlobalSearch": true,
  "headerThemeSwitcher": true,
  "headerUserProfile": true
}
```

**Notes:**
- All flags are boolean. Unknown keys are silently ignored by the frontend.
- The response can be a partial object. For example, returning `{ "viewChat": false }` disables only the chat view while all other features remain enabled.
- The frontend fetches flags once on application load. If the request fails (network error, 401, 500, etc.), all flags default to `true`.

---

## Endpoint Summary

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/schema/tree` | Full schema hierarchy |
| GET | `/api/v1/schema/entities/{id}` | Single entity lookup |
| GET | `/api/v1/schema/entities/{id}/facets` | Entity facets (descriptive, structural, relations) |
| GET | `/api/v1/concepts` | List concepts (optional `?category=` or `?tag=` filter) |
| GET | `/api/v1/concepts/{id}` | Single concept |
| GET | `/api/v1/concepts/categories` | Category list with counts |
| GET | `/api/v1/concepts/tags` | Tag list with counts |
| GET | `/api/v1/queries` | List saved queries |
| GET | `/api/v1/queries/{id}` | Single saved query |
| POST | `/api/v1/queries/execute` | Execute SQL, return results |
| GET | `/api/v1/conversations` | List conversations |
| POST | `/api/v1/conversations` | Create conversation |
| DELETE | `/api/v1/conversations/{id}` | Delete conversation |
| POST | `/api/v1/conversations/{id}/messages` | Send message, stream response (SSE) |
| POST | `/api/v1/inline-chat/messages` | Context-aware chat, stream response (SSE) |
| GET | `/api/v1/stats` | Dashboard summary counts |
| GET | `/api/v1/search?q=...` | Cross-domain search (views, schema, concepts, queries) |
| GET | `/api/v1/features` | Feature flags for current user/session |
