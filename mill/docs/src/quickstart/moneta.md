# Moneta Quickstart

This quickstart demonstrates the natural language to SQL (NL2SQL) capabilities of **Mill**, using a lightweight, chat-based interface.  
It enables you to explore, visualize, and describe structured data using plain English queries — powered by Large Language Models (LLMs).

The sample data is based on a fictional financial domain and includes:

- Client information
- Loan details and payment history
- Portfolio and stock trade activity

Use the Moneta chat interface to interact with the data and gain deeper insights.  
This quickstart is designed to provide a hands-on experience — so dive in!

To run Moneta locally, you need to have [Docker](https://www.docker.com/) installed.  
Docker enables you to launch Moneta in an isolated container without modifying your system.

Follow the installation guide for your operating system:

- **Windows**: [Install Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/)
- **Linux**: [Install Docker Engine on Linux](https://docs.docker.com/engine/install/)
- **macOS**: [Install Docker Desktop for Mac](https://docs.docker.com/desktop/install/mac-install/)


## Running Moneta

Moneta can run using different LLM providers.  

### OpenAI

To use Moneta with OpenAI, you’ll need an OpenAI API key and to choose an available chat model.

#### Get an OpenAI API Key

1. Sign in to [OpenAI Platform](https://platform.openai.com/)
2. Go to your [API Keys page](https://platform.openai.com/account/api-keys)
3. Click **Create new secret key** and copy it securely

#### Supported Chat Models

You can use any of the following models in Moneta:

- `gpt-4o`
- `gpt-4o-mini`
- `gpt-4-turbo`
- `gpt-3.5-turbo`

For the full list of available models and pricing, see [OpenAI's model overview](https://platform.openai.com/docs/models).

Run the following command, replacing the placeholders with your actual OpenAI values:

/// tab | bash (Linux, macOS)
```bash
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:v0.5.0-dev \
  --spring.ai.model.chat=openai \
  --spring.ai.openai.api-key=<your-openai-api-key> \
  --spring.ai.openai.chat.options.model=<chat-model>
```
///

/// tab | PowerShell (Windows)
```powershell
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:v0.5.0-dev `
  --spring.ai.model.chat=openai `
  --spring.ai.openai.api-key=<your-openai-api-key>` 
  --spring.ai.openai.chat.options.model=<chat-model>
```
///

Once running, open [http://localhost:8080](http://localhost:8080) in your browser to begin querying your data using OpenAI's LLMs.

### Azure OpenAI

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
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:v0.5.0-dev \
  --spring.ai.model.chat=azure-openai \
  --spring.ai.azure.openai.api-key=<your-azure-open-ai-key> \
  --spring.ai.azure.openai.endpoint=<your-azure-open-ai-endpoint> \
  --spring.ai.azure.openai.chat.options.deployment-name=<your-azure-open-ai-deployment-name>
```
///

/// tab | PowerShell (Windows)
```powershell
docker run -ti -p "8080:8080" qpointz/mill-service-moneta:v0.5.0-dev `
  --spring.ai.model.chat=azure-openai `
  --spring.ai.azure.openai.api-key="<your-azure-open-ai-key>" `
  --spring.ai.azure.openai.endpoint="<your-azure-open-ai-endpoint>" `
  --spring.ai.azure.openai.chat.options.deployment-name="<your-azure-open-ai-deployment-name>"
```
///

Once started, open your browser and visit: [http://localhost:8080](http://localhost:8080)

You can now interact with the Moneta chat interface using natural language queries.
