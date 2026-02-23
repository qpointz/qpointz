# Grinder UI

The Grinder UI is a web-based interface for querying data with natural language, browsing the data model, and managing business context. It is served by Mill at the application root path.

---

## Getting Started

The interface has three main sections accessible from the sidebar:

- **Chat** — Ask questions about your data in plain language
- **Data Model** — Browse and explore your database structure
- **Context** — View and manage business concepts and definitions

The interface supports both light and dark themes, which you can toggle using the theme switcher in the header.

---

## Chat View

The Chat view is where you interact with Mill using natural language.

### Starting a Conversation

When you first open the Chat view, you can:

- **Start a new chat** — Begin a fresh conversation
- **Continue an existing chat** — Resume a previous conversation from the sidebar

### Asking Questions

Type your question in the message input at the bottom of the screen. Examples:

- *"Show me all clients in Switzerland"*
- *"What's the total revenue for last quarter?"*
- *"Display a bar chart of monthly sales"*
- *"Explain what the orders table contains"*

### Chat Input Features

The chat input includes features to help you interact with your data:

#### Command Palette (`/`)

Type `/` at the start of your message to access available commands:

- **`/get-data`** — Retrieve tabular data with filters
- **`/get-chart`** — Visualize data as a chart
- **`/explain`** — Get explanations about tables, queries, or results
- **`/refine`** — Modify a previous query
- **`/do-conversation`** — Have a casual conversation
- **`/enrich-model`** — Add domain knowledge to improve future queries

Use arrow keys to navigate, Enter to select, or Escape to cancel.

#### @ Mentions (`@`)

Type `@` to search and reference tables or columns directly in your message:

- Type `@` followed by a table or column name
- Search results appear automatically
- Select an entity to insert it into your message

Example: *"Show me all records from @clients where @country equals 'Switzerland'"*

### Understanding Responses

Responses are organized by **intent type**, each displayed in a card format:

#### Get Data Intent

When you ask for data, you'll see:

- **SQL Query** — The generated SQL query (click to copy)
- **Data Table** — Scrollable table with your results
- **Query Name** — A file-safe name for the query (useful for exports)

#### Get Chart Intent

When you request a visualization, you'll see:

- **Chart** — Interactive visualization of your data
- **Chart Type** — Automatically selected or as specified
- **SQL Query** — The query used to generate the chart data

#### Explain Intent

When you ask for explanations, you'll receive:

- **Natural language explanation** — Description of tables, queries, or results
- **Context** — Relevant information to help you understand the data

#### Clarification Requests

Sometimes Mill needs more information to answer your question. When this happens:

- **Clarification questions** appear in a highlighted card
- **Answer directly** in the chat input
- **Cancel** if you want to rephrase your question

The status indicator at the top of the input shows when clarification is needed.

### Managing Chats

In the sidebar, you can:

- **View all chats** — See your conversation history
- **Create new chat** — Start a fresh conversation
- **Delete chats** — Remove conversations you no longer need
- **Switch between chats** — Click any chat to continue it

Each chat maintains its own conversation history, so you can have multiple conversations about different topics.

---

## Data Model View

The Data Model view lets you explore and understand your database structure visually.

### Navigation Tree

The sidebar shows a hierarchical tree of your data:

- **Schemas** — Top-level groupings (expandable)
- **Tables** — Database tables within each schema
- **Attributes** — Columns within each table

Click any item to view its details in the main panel.

### Entity Details

When you select a schema, table, or attribute, the main panel displays:

#### Entity Header

At the top:

- **Entity name and type** — Identification with icons
- **Location** — Full path (e.g., `schema.table.attribute`)
- **Structural information** — Data types, constraints, keys
- **Scope selector** — View metadata for different scopes (global, user, team, role)

#### Facets

Metadata is organized into **facets**:

- **Descriptive** — Human-readable descriptions, business meaning, tags, ownership
- **Structural** — Physical database details (data types, constraints, nullability)
- **Relations** — How tables connect to each other
- **Value Mappings** — How user-friendly terms map to database values
- **Concepts** — Business concepts related to this entity

Each facet is displayed in its own section with labels and organized information.

#### Related Items

At the bottom:

- **Related tables** — Tables connected to this entity
- **Related attributes** — Columns that relate to this entity
- **Related concepts** — Business concepts that reference this entity

Click any related item to navigate to it.

### Searching

Use the search functionality to find:

- Tables by name
- Columns by name
- Entities by description or tags

Search results show the entity type, location, and description.

### URL Sharing

The URL updates as you navigate (e.g., `/data-model/schema/table/attribute`), allowing you to:

- **Bookmark** specific entities
- **Share links** with team members
- **Use browser navigation** (back/forward)

---

## Context View

The Context view displays business concepts — high-level definitions that span multiple tables and help Mill understand your domain.

### Organizing Contexts

Contexts are organized by:

- **Categories** — Group related concepts together
- **Tags** — Flexible labeling for filtering

Click any category or tag to filter contexts.

### Viewing Contexts

The main panel shows:

- **Context name** — The concept identifier
- **Description** — What the concept represents
- **Related entities** — Tables and attributes this concept applies to
- **SQL definitions** — Optional SQL expressions that define the concept

### Using Contexts

Contexts help Mill understand your business terminology:

- When you say "premium customers," Mill knows which tables and conditions apply
- Concepts can span multiple tables and define complex business rules
- They improve the accuracy of natural language queries

---

## Interface Features

### Theme Toggle

Switch between light and dark themes using the theme toggle in the header. Your preference is saved automatically.

### Responsive Design

The interface adapts to different screen sizes:

- **Desktop** — Full sidebar and main panel
- **Tablet/Mobile** — Collapsible sidebar with hamburger menu

### Keyboard Shortcuts

- **Ctrl+Enter** — Send message (in chat input)
- **Arrow keys** — Navigate command palette and @ mentions
- **Enter** — Select item in menus
- **Escape** — Cancel menus or dialogs

### Status Indicators

The interface provides feedback through:

- **Loading states** — Spinners and progress indicators
- **Status messages** — Notifications about what is happening
- **Error messages** — Information when something goes wrong

---

## Tips for Best Results

### Writing Good Questions

- **Be specific** — "Show me clients in Switzerland" is better than "show clients"
- **Use business terms** — Mill understands domain language if metadata is configured
- **Reference tables** — Use @ mentions to be explicit about which data you want
- **Ask follow-ups** — Build on previous questions for deeper exploration

### Understanding Responses

- **Review the SQL** — See exactly what query was generated
- **Check the data** — Verify results match your expectations
- **Read explanations** — Use explain intents to understand unfamiliar tables
- **Explore relationships** — Use the Data Model view to understand connections

### Managing Metadata

- **Add descriptions** — Help Mill understand your data better
- **Define value mappings** — Map business terms to database values
- **Create concepts** — Define high-level business concepts
- **Document relationships** — Explain how tables connect

---

## Common Use Cases

### Exploring New Data

1. Start in **Data Model** view to understand the structure
2. Read entity descriptions to learn what each table contains
3. Use **Chat** to ask exploratory questions
4. Review generated SQL to understand how queries work

### Answering Business Questions

1. Go to **Chat** view
2. Ask your question in plain language
3. Review the results
4. Ask follow-up questions to refine or expand

### Understanding Relationships

1. Navigate to a table in **Data Model** view
2. Check the **Relations** facet
3. Click related items to explore connections
4. Use **Context** view to see business-level relationships

### Sharing Insights

1. Navigate to the relevant entity or chat
2. Copy the URL from your browser
3. Share with team members
4. They'll see exactly what you're viewing

---

## Troubleshooting

- **No results?** Check that your question is clear and references existing tables
- **Wrong data?** Review the SQL query to see what was generated
- **Can't find a table?** Use the search function in Data Model view
- **Clarification needed?** Answer the questions Mill asks to get better results
