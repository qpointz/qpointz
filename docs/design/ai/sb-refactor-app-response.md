---
name: Unify Reasoner Response Types with ChatApplicationResponse
overview: Create ChatApplicationResponse interface with ChatCallResponse and ContentResponse variants. Reasoner returns ChatApplicationResponse, and intent mapping is done in Reasoner. ChatApplication.query() returns ChatApplicationResponse.
todos:
  - id: create-chat-application-response
    content: Create ChatApplicationResponse sealed interface with ChatCallResponse and ContentResponse implementations
    status: pending
  - id: update-reasoner-interface
    content: Update Reasoner interface to return ChatApplicationResponse instead of ChatCall
    status: pending
  - id: update-default-reasoner
    content: Update DefaultReasoner to return ChatCallResponse wrapping the intent ChatCall (mapping done in reasoner)
    status: pending
  - id: update-stepback-reasoner
    content: Update StepBackReasoner to return ContentResponse when clarification needed, or ChatCallResponse when ready (mapping done in reasoner)
    status: pending
  - id: update-chat-application
    content: Update ChatApplication.query() to return ChatApplicationResponse, removing intent mapping logic
    status: pending
  - id: update-tests
    content: Update tests to work with ChatApplicationResponse
    status: pending
---

# Unify Reasoner Response Types with ChatApplicationResponse

## Problem Analysis

Currently, the two reasoner implementations return incompatible structures:

1. **DefaultReasoner**: Returns `ChatCall` → `ChatApplication` deserializes to `ReasoningResponse` and maps to intent
2. **StepBackReasoner**: Returns `StepBackReasoningCall` → when clarification needed, returns `StepBackResponse` (no `intent`), when ready, returns merged map with `intent`

`ChatApplication.query()` assumes it can always deserialize to `ReasoningResponse.class` and does intent mapping, which fails when StepBackReasoner needs clarification.

## Solution Overview

Create a unified `ChatApplicationResponse` interface that can represent both cases:

- **ChatCallResponse**: Wraps a `ChatCall` (when ready to execute intent)
- **ContentResponse**: Wraps `Map<String, Object>` content (when clarification needed)

Key changes:

1. **Reasoner** returns `ChatApplicationResponse` (not `ChatCall`)
2. **Intent mapping** is done in `Reasoner` implementations (not in `ChatApplication`)
3. **ChatApplication.query()** returns `ChatApplicationResponse` directly

This provides clear separation: reasoners decide what to return, and `ChatApplication` just passes it through.

## Implementation Plan

### 1. Create ChatApplicationResponse Interface

**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplicationResponse.java`

Create a sealed interface with two implementations:

```java
public sealed interface ChatApplicationResponse 
    permits ChatApplicationResponse.ChatCallResponse, 
            ChatApplicationResponse.ContentResponse {
    
    record ChatCallResponse(ChatCall call) implements ChatApplicationResponse {}
    
    record ContentResponse(Map<String, Object> content) implements ChatApplicationResponse {}
}
```

### 2. Update Reasoner Interface

**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/Reasoner.java`

Change the interface to return `ChatApplicationResponse`:

```java
public interface Reasoner {
    ChatApplicationResponse reason(ChatUserRequest request);
}
```

Remove the nested `ReasoningResult` record if it exists (or keep it for backward compatibility if needed elsewhere).

### 3. Update DefaultReasoner

**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/reasoners/DefaultReasoner.java`

Update to:

1. Execute `ReasonCall` to get `ReasoningResponse`
2. Map to intent using `IntentSpecs` (move logic from `ChatApplication.getIntentCall()`)
3. Return `ChatCallResponse` wrapping the intent `ChatCall`

The reasoner now needs access to `IntentSpecs` to do the mapping.

### 4. Update StepBackReasoner

**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/reasoners/StepBackReasoner.java`

Update to:

1. Execute `StepBackReasoningCall` to get result
2. Check if clarification needed:

   - If `need-clarification=true`: Return `ContentResponse` with step-back response map
   - If `need-clarification=false`: 
     - Extract `ReasoningResponse` from merged result
     - Map to intent using `IntentSpecs`
     - Return `ChatCallResponse` wrapping the intent `ChatCall`

The reasoner now needs access to `IntentSpecs` to do the mapping.

### 5. Update ChatApplication

**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplication.java`

Simplify `ChatApplication`:

- `query()` method: Just call `reasoner.reason(request)` and return the `ChatApplicationResponse`
- Remove `getIntentCall()` method (moved to reasoners)
- `reason()` method: Can be removed or kept for backward compatibility

### 6. Update Reasoner Constructors

Both reasoner implementations need access to `IntentSpecs`:

- **DefaultReasoner**: Add `IntentSpecs` parameter to constructor
- **StepBackReasoner**: Add `IntentSpecs` parameter to constructor

### 7. Update Tests

Update integration tests to:

- Work with `ChatApplicationResponse`
- Use pattern matching to handle `ChatCallResponse` vs `ContentResponse`
- Extract `ChatCall` from `ChatCallResponse` when needed

## Key Design Decisions

1. **Sealed Interface**: Provides type safety and clear intent separation
2. **Intent Mapping in Reasoner**: Reasoners are responsible for mapping to intents, not `ChatApplication`
3. **Clear Separation**: `ChatApplication` becomes a thin wrapper that just delegates to reasoner
4. **Backward Compatibility**: May need to keep old `reason()` method returning `ChatCall` if used elsewhere
5. **Dependency Injection**: Reasoners need `IntentSpecs`, which may require updating factory/configuration code

## Files to Modify

- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplicationResponse.java` (NEW)
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/Reasoner.java` - Change return type
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/reasoners/DefaultReasoner.java` - Add IntentSpecs, do intent mapping
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/reasoners/StepBackReasoner.java` - Add IntentSpecs, do intent mapping
- `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplication.java` - Simplify, remove intent mapping
- Configuration/factory code that creates reasoners (to inject IntentSpecs)
- Test files that use `ChatApplication.query()` or reasoner methods

