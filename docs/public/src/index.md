# Mill: Your AI-Powered Data Assistant

**Ask questions in plain English. Get instant answers from your data.**

Mill transforms how you interact with data by letting you ask questions naturally—no SQL knowledge required. Whether you're exploring customer trends, analyzing sales performance, or investigating business metrics, Mill understands what you need and delivers the answers instantly.

---

## 🚀 What Makes Mill Special

### Talk to Your Data in Plain English

Simply ask questions like:
- *"Show me all premium customers in Switzerland"*
- *"What's the monthly revenue trend for the last quarter?"*
- *"Which products have inventory below 10 units?"*

Mill's AI-powered assistant understands your intent, translates it to the right queries, and returns results in tables, charts, or clear explanations—exactly what you need, when you need it.

### One Platform, All Your Data

Mill connects to everything:
- **Databases**: PostgreSQL, MySQL, SQL Server, Oracle, Snowflake, and more
- **Files**: CSV, JSON, Parquet, Avro, and other structured formats
- **Cloud Services**: Connect to your existing data infrastructure

Query across multiple sources as if they were one unified database. No more switching between tools or learning different query languages.

### Intelligent Understanding

Mill doesn't just execute queries—it understands context:

- **Smart Value Mapping**: When you say "premium customers," Mill knows you mean customers with `segment = 'PREMIUM'` in your database
- **Clarification When Needed**: If your question is ambiguous, Mill asks clarifying questions to ensure you get exactly what you're looking for
- **Business Context**: Mill learns your domain terminology and business concepts, making queries more accurate over time

### Beautiful, Interactive Interface

The Mill Grinder UI provides a modern, intuitive experience:

- **Chat Interface**: Have conversations with your data, just like chatting with a colleague
- **Data Model Explorer**: Browse your database structure visually, understand relationships, and discover available data
- **Context Management**: Organize and share business concepts, definitions, and domain knowledge
- **Dark Mode**: Work comfortably in any lighting condition

---

## 💡 Key Capabilities

### Natural Language Queries

Ask questions naturally and get structured results. Mill supports multiple intent types:

- **Get Data**: Retrieve tabular results with filters and conditions
- **Get Charts**: Request visualizations—bar charts, line graphs, pie charts—automatically generated
- **Explain**: Understand what tables mean, how queries work, or what results represent
- **Refine**: Modify previous queries with follow-up questions

### Metadata Management

Enrich your data with business context:

- **Descriptions & Documentation**: Add human-readable descriptions to tables and columns
- **Business Concepts**: Define high-level concepts that span multiple tables
- **Value Mappings**: Map user-friendly terms (like "premium") to database values
- **Relationships**: Document how tables connect and relate to each other
- **Tags & Classification**: Organize and categorize your data assets

The more metadata you add, the smarter Mill becomes at understanding your questions.

### Enterprise-Grade Security

Protect your data with fine-grained access control:

- **Role-Based Access**: Control who can see what data
- **Row-Level Security**: Filter data based on user roles or attributes
- **Column-Level Protection**: Hide sensitive columns from unauthorized users
- **Multiple Authentication Methods**: Support for OAuth2, JWT tokens, Microsoft Entra ID, and basic authentication

### Flexible Integration

Use Mill the way that works best for you:

- **REST API**: Integrate with any application or tool
- **JDBC Driver**: Connect from your favorite SQL tools, BI platforms, or applications
- **Python Client**: Use Mill in your data science workflows and Jupyter notebooks

### Multiple Deployment Options

Deploy Mill however fits your infrastructure:

- **Docker**: Quick setup with containerized deployment
- **Docker Compose**: Orchestrate multiple services easily
- **Kubernetes**: Scale across clusters for enterprise needs

---

## 🎯 Who Is Mill For?

### Business Analysts

Stop waiting for SQL experts. Ask questions directly and get answers immediately. Explore data, validate assumptions, and prepare reports—all without writing a single line of SQL.

### Data Teams

Provide self-service analytics to your business users while maintaining security and governance. Mill handles the complexity, so you can focus on higher-value work.

### Domain Experts

Use your business terminology naturally. Mill learns your domain language and understands concepts like "premium customers" or "high-value transactions" without technical translation.

### Developers

Integrate Mill into your applications via REST APIs or JDBC drivers. Build data-driven features without managing complex query logic.

---

## 🚀 Getting Started

Getting started with Mill is simple:

1. **Install**: Deploy Mill using Docker or your preferred method
2. **Connect**: Point Mill to your data sources
3. **Ask**: Start asking questions in plain English

For detailed setup instructions, see our [Installation Guide](installation.md).

Ready to explore? Try our [Quickstart Guide](quickstart.md) to see Mill in action with sample data.

---

## 🌟 Why Choose Mill?

### No SQL Required

Mill eliminates the barrier between you and your data. Ask questions naturally, and get answers instantly—no technical expertise needed.

### Works with Your Existing Data

Connect to your current databases and files. No data migration, no new infrastructure. Mill works with what you already have.

### Gets Smarter Over Time

As you add metadata and use Mill, it learns your business terminology and becomes more accurate. Your investment in documentation pays off with better results.

### Enterprise Ready

Built for production with security, scalability, and reliability in mind. Deploy with confidence.

---

## 📚 Learn More

- **[Quickstart Guide](quickstart.md)**: Get up and running quickly
- **[Installation Guide](installation.md)**: Detailed setup instructions
- **Chat Assistant Intents**: Learn what you can ask Mill to do (see reference documentation in `docs/public_old/`)

---

**Ready to transform how you work with data?** [Get started today →](quickstart.md)
