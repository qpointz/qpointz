# Quickstart

Get started with Mill in minutes. Choose the option that works best for you.

---

## 🚀 Moneta: Ready-to-Use Example

**The fastest way to experience Mill** — Moneta is a complete, pre-configured example that includes sample data and a chat interface. Perfect for exploring Mill's capabilities without any setup.

### What is Moneta?

Moneta is a ready-to-use Mill deployment that demonstrates natural language to SQL (NL2SQL) capabilities using a lightweight, chat-based assistant.

Our AI-powered data chat assistant enables business users, analysts, and domain experts to ask questions about their data using plain language — and instantly receive structured, reliable answers in the form of SQL queries, charts, or clear explanations.

It intelligently understands user intent (e.g., "get data," "visualize trends," "explain a query"), identifies the necessary tables and relationships, and determines the optimal way to inject schema context — ensuring both performance and precision.

Designed for flexibility, it supports both reasoning and retrieval-augmented (RAG) workflows, allowing teams to scale from small schemas to complex data models. Users can also enrich the system with domain knowledge and generate consistent, filesystem-safe query names for exports and reproducibility.

Whether you're exploring KPIs, validating assumptions, or preparing reports — this assistant turns your data into actionable answers without needing technical expertise.

### Sample Data

The Moneta example includes a fictional financial domain with:

- **Client information** — Customer profiles and details
- **Loan details** — Loan records and payment history
- **Portfolio and stock trade activity** — Investment and trading data

**Important:** All data in Moneta is fully fictional and synthetic — it is generated solely for demonstration purposes and does not represent any real individuals, companies, or financial transactions.

### Prerequisites

To run Moneta locally, you need to have [Docker](https://www.docker.com/) installed. Docker enables you to launch Moneta in an isolated container without modifying your system.

Follow the installation guide for your operating system:

- **Windows**: [Install Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/)
- **Linux**: [Install Docker Engine on Linux](https://docs.docker.com/engine/install/)
- **macOS**: [Install Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)

### Running Moneta

Moneta can run using different LLM providers. Choose the one that works best for you.

#### OpenAI

To use Moneta with OpenAI, you'll need an OpenAI API key and to choose an available chat model.

##### Get an OpenAI API Key

1. Sign in to [OpenAI Platform](https://platform.openai.com/)
2. Go to your [API Keys page](https://platform.openai.com/account/api-keys)
3. Click **Create new secret key** and copy it securely

##### Supported Chat Models

You can use any of the following models in Moneta:

- `gpt-4o`
- `gpt-4o-mini`
- `gpt-4-turbo`
- `gpt-3.5-turbo`

For the full list of available models and pricing, see [OpenAI's model overview](https://platform.openai.com/docs/models).

Run the following command, replacing the placeholders with your actual OpenAI values:

/// tab | bash (Linux, macOS)
```bash
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:**{{ version }}** \
  --spring.ai.model.chat=openai \
  --spring.ai.openai.api-key=<your-openai-api-key> \
  --spring.ai.openai.chat.options.model=<chat-model>
```
///

/// tab | PowerShell (Windows)
```powershell
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:**{{ version }}** `
  --spring.ai.model.chat=openai `
  --spring.ai.openai.api-key=<your-openai-api-key>` 
  --spring.ai.openai.chat.options.model=<chat-model>
```
///

Once running, open [http://localhost:8080](http://localhost:8080) in your browser to begin querying your data using OpenAI's LLMs.

#### Azure OpenAI

To connect Moneta with Azure OpenAI:

1. **Create an Azure OpenAI resource**  
   Go to the Azure Portal and follow this guide:  
   [Quickstart: Get started with Azure OpenAI](https://learn.microsoft.com/en-us/azure/cognitive-services/openai/quickstart)

2. **Deploy a chat model**
   - Navigate to your Azure OpenAI resource
   - Open the **Deployments** tab
   - Deploy a model like `gpt-4` or `gpt-35-turbo`

3. **Gather API credentials**
   - Copy your **API key**
   - Note your **endpoint URL**
   - Record the **deployment name**

4. **Launch Mill Moneta example**

Run the following command, replacing the placeholders with your actual Azure OpenAI values:

/// tab | bash (Linux, macOS)
```bash
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:**{{ version }}** \
  --spring.ai.model.chat=azure-openai \
  --spring.ai.azure.openai.api-key=<your-azure-open-ai-key> \
  --spring.ai.azure.openai.endpoint=<your-azure-open-ai-endpoint> \
  --spring.ai.azure.openai.chat.options.deployment-name=<your-azure-open-ai-deployment-name>
```
///

/// tab | PowerShell (Windows)
```powershell
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:**{{ version }}** `
  --spring.ai.model.chat=azure-openai `
  --spring.ai.azure.openai.api-key="<your-azure-open-ai-key>" `
  --spring.ai.azure.openai.endpoint="<your-azure-open-ai-endpoint>" `
  --spring.ai.azure.openai.chat.options.deployment-name="<your-azure-open-ai-deployment-name>"
```
///

Once started, open your browser and visit: [http://localhost:8080](http://localhost:8080)

You can now interact with the Moneta chat interface using natural language queries.

### Try It Out

Once Moneta is running, try asking questions like:

- *"Show me all clients in Switzerland"*
- *"What's the total loan amount by country?"*
- *"Display a chart of monthly payment trends"*
- *"Explain what the clients table contains"*

The chat interface will understand your questions, generate the appropriate SQL queries, and return results in tables, charts, or explanations.

---

## Other Quickstart Options

### Custom Setup

If you want to connect Mill to your own data sources, see the [Installation Guide](installation.md) for detailed setup instructions.

### Sandbox Environments

Coming soon: Additional sandbox environments for exploring different use cases:

- **Calcite Sandbox** — Query flat files and federated data sources
- **JDBC Sandbox** — Connect to various databases with included drivers
- **SQL Line Shell** — Command-line interface for direct SQL execution

---

**Ready to explore?** Start with Moneta to see Mill in action, then customize it for your own data sources.
