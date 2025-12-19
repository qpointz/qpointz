# Modular LLM Architecture v2  
## Capabilities, Advisors, Orchestrators, and Reasoner

This document is an updated design summary that incorporates the refined understanding of **Reasoner**, its scope, and its interaction with **Capabilities**, **Intents**, and **Orchestrators**.

The architectural goal is to build an extensible LLM platform where **understanding**, **decision-making**, and **execution** are strictly separated, and where concrete solutions like NL2SQL continuously *shrink* instead of growing into monolithic systems.

---

## 1. Capability

A **Capability** is the fundamental modular unit of system functionality.

**Definition**

A Capability encapsulates *one coherent system ability* and declares everything required for the LLM to use that ability correctly.

A Capability may include:
- **Advisors** – behavioral rules and constraints
- **Tools / Function Callbacks** – access to authoritative data
- **Policies / validation hooks** – enforcement outside the LLM
- **Reasoner Descriptions** – declarations of supported task classes

**What a Capability must NOT do**
- It does not know about specific chats (e.g. NL2SQL)
- It does not select intents
- It does not call other capabilities
- It does not orchestrate workflows

Capabilities are **passive**, **self-describing**, and **reusable**.

Examples:
- SchemaDiscoveryCapability
- ValueMappingCapability
- SqlValidationCapability
- SchemaEnrichmentCapability
- GetDataCapability
- GetChartCapability

---

## 2. Advisor

An **Advisor** defines *how the LLM should behave* when a Capability is active.

**Definition**

An Advisor constrains or guides the LLM’s reasoning and output style, but does not provide facts.

Typical responsibilities:
- Enforce non-guessing rules
- Explain when tools must be used
- Define domain or SQL constraints
- Provide guardrails against hallucination

Advisors:
- Never provide authoritative data
- Never execute logic
- Are always attached through Capabilities

---

## 3. Tools (Function Callbacks)

**Tools** provide **ground truth**.

**Definition**

A Tool is the only allowed mechanism for retrieving factual, system-owned information.

Examples:
- Table catalogs
- Column definitions
- Relationships
- Value mappings
- Supported SQL dialect features

Design principles:
- Narrow and explicit purpose
- Structured input and output
- No “return everything” except for small catalogs
- Detailed information only on demand

Tools replace schema injection in prompts.

---

## 4. Reasoner

### 4.1 What the Reasoner IS

The **Reasoner** is a *pre-execution analytical step* whose sole responsibility is to determine **what the user wants to do**, not **how it will be done**.

The Reasoner:
- Interprets user input
- Classifies the request into an abstract **task class**
- Operates only within the space of tasks declared by active Capabilities
- May return ambiguity or low confidence

The Reasoner:
- Does not reason about schemas
- Does not select tables
- Does not plan execution
- Does not know about Tools
- Does not produce SQL or execution hints

---

## 5. Reasoner Descriptions

Each Capability may declare one or more **Reasoner Descriptions**.

**Purpose**

Reasoner Descriptions declare *what kinds of user tasks are possible* when this Capability is active.

They are:
- Declarative
- Human-oriented
- Free of technical or implementation details
- Independent of intent names

**Example**

Task class: data retrieval  
Description: Retrieve structured data from the system for analysis or inspection.  
Examples:
- Show total revenue by country
- List top 10 customers

Task class: data visualization  
Description: Produce visual representations (charts) based on aggregated data.  
Examples:
- Show a chart of revenue by country
- Plot sales over time

If a Capability is not active, its Reasoner Descriptions are not included in the Reasoner prompt.

---

## 6. Reasoner Prompt Construction

The Reasoner prompt is assembled dynamically:

1. The active profile or orchestrator selects Capabilities
2. All Reasoner Descriptions from those Capabilities are collected
3. The Reasoner sees only the supported task space

This guarantees:
- No hardcoded intent vocabulary
- No global task taxonomy
- Full alignment with system capabilities

---

## 7. Reasoner Output (Final Contract)

The Reasoner output is intentionally minimal.

Example:

{
  "taskClass": "data_visualization",
  "confidence": 0.85,
  "explanation": "User explicitly asked for a chart"
}

Optional fields:
- ambiguity: low | high
- clarificationNeeded: true

No schema, table, SQL, or execution-related information appears here.

---

## 8. Intent

### 8.1 What an Intent Is

An **Intent** is a **system-level execution contract** that defines *what will be executed*.

An Intent:
- Belongs to a closed, system-controlled set
- Describes execution semantics
- Knows which Capabilities are required
- Is never exposed to the Reasoner

Example (conceptual):

intent: get_chart  
description: Retrieve data and produce a visualization

---

## 9. Mapping Task Class to Intent

After reasoning:

1. The system receives a task class
2. It checks the active profile
3. It deterministically maps the task class to an Intent

This mapping:
- Is explicit and testable
- Does not require an LLM
- May be profile-specific

LLM assistance is optional and only for ambiguity resolution, never for final authority.

---

## 10. Orchestrator

The **Orchestrator** is responsible for selecting the execution path, not for understanding the user.

Responsibilities:
- Validate that a task is supported in the active profile
- Map task class to Intent
- Activate the required Capabilities
- Build the execution ChatClient (advisors + tools)

Non-responsibilities:
- No user reasoning
- No schema logic
- No business logic beyond wiring

Temporary if/else orchestration is acceptable at early stages.

---

## 11. Chat Profiles / Modes

A **Chat Profile** defines:
- Which Capabilities are active
- Which task classes are exposed to the Reasoner
- Which Intents are allowed

Profiles can be switched within the same chat session.

Rules:
- Message history remains
- Capability-specific context may be reset or isolated
- LLM never switches profiles autonomously

---

## 12. End-to-End Flow

User input  
→ Reasoner (WHAT the user wants)  
→ Task class  
→ System mapping  
→ Intent (WHAT will be executed)  
→ Orchestrator  
→ Capability set  
→ Execution LLM (Advisors + Tools)  
→ Result

No schema knowledge appears before execution.  
No execution planning appears during reasoning.

---

## 13. Core Architectural Principles

- Reasoner answers WHAT
- Capabilities define WHAT IS POSSIBLE
- Intents define WHAT WILL BE DONE
- Orchestrator wires WHO DOES WHAT
- Tools define WHAT IS TRUE
- NL2SQL is configuration, not logic
- Prompts shrink, Capabilities grow

---

## One-Sentence Summary

The Reasoner classifies user intent within the space declared by active Capabilities; the system—not the LLM—decides how that intent is executed.
