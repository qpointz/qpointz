# Step-Back UX Implementation Plan
## Aligning Implementation with sb-ux-flow.md

This document outlines the changes needed to implement the user experience flows defined in `sb-ux-flow.md`.

---

## Current State Analysis

### Backend (AI Core)
- ✅ Step-Back reasoning is implemented and working
- ✅ Clarification questions are generated
- ✅ Blocking logic prevents SQL generation when needed
- ❌ **Gap**: No user-facing natural language messages generated
- ❌ **Gap**: Explanation field is technical, not user-friendly
- ❌ **Gap**: No distinction between internal reasoning and user messages

### Backend (Chat Service)
- ✅ Extracts explanation from response
- ✅ Sets message field from explanation
- ❌ **Gap**: Uses technical explanation directly, not user-friendly messages
- ❌ **Gap**: No special handling for clarification states

### Frontend (UI)
- ✅ StepBackCard displays step-back details
- ✅ Shows clarification questions with Reply buttons
- ❌ **Gap**: Shows technical details (Step-Back, Ambiguities, Verification) to users
- ❌ **Gap**: No natural language clarification introduction
- ❌ **Gap**: StepBackCard visible even for simple queries
- ❌ **Gap**: Explanation text only shown in intent cards, not as chat bubbles

---

## Target State (from sb-ux-flow.md)

### Simple Query Flow
- User sees: "Sure! Calculating total revenue for last month…"
- Then: Results (table/chart) + "Here are the results"
- **Step-Back is invisible** to the user

### Ambiguous Query Flow
- User sees: "To answer this accurately, I need a quick clarification:"
- Then: Natural question list (no technical jargon)
- After answer: "Great! Fetching active users based on your definition…"
- Then: Results + "Here's what I found"

### Tone Requirements
- Polite, expert, conversational
- Non-technical by default
- No SQL jargon unless requested
- Natural analyst language

---

## Implementation Plan

### Phase 1: Backend - User-Facing Message Generation

#### 1.1 Add User Message Field to Step-Back Response
**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/models/stepback/StepBackResponse.java`

Add a new field:
```java
@JsonProperty("user-message")
String userMessage; // Natural language message for the user
```

**Purpose**: Store user-friendly messages separate from technical reasoning.

#### 1.2 Update Step-Back Prompt to Generate User Messages
**File**: `ai/mill-ai-core/src/main/resources/templates/nlsql/stepback/system.prompt`

Add guidance:
- Generate natural, conversational user messages
- Use polite analyst tone
- Avoid technical jargon
- Mirror user's language

**File**: `ai/mill-ai-core/src/main/resources/templates/nlsql/stepback/user.prompt`

Add to JSON structure:
```json
{
  "step-back": { ... },
  "clarification": {
    "need-clarification": true | false,
    "questions": [...],
    "user-message": "To answer this accurately, I need a quick clarification:"
  },
  "reasoning": {
    ...
    "user-message": "Sure! Calculating total revenue for last month..."
  }
}
```

#### 1.3 Update ChatApplication to Generate User Messages
**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplication.java`

**Changes**:
1. When blocked (clarification needed):
   - Extract `clarification.user-message` or generate default: "To answer this accurately, I need a quick clarification:"
   - Set as top-level `user-message` in response

2. When not blocked (simple query):
   - Extract `reasoning.user-message` or generate default based on intent
   - For get-data/get-chart: "Sure! Calculating..." or "Here are the results"
   - Set as top-level `user-message` in response

3. After clarification resolved:
   - Generate: "Great! Fetching [query description] based on your definition..."
   - Set as top-level `user-message`

#### 1.4 Update ChatProcessor to Use User Messages
**File**: `ai/mill-ai-nlsql-chat-service/src/main/java/io/qpointz/mill/ai/nlsql/components/ChatProcessor.java`

**Changes**:
1. Prioritize `user-message` over `explanation` for the message field
2. Fallback logic:
   ```java
   val userMessage = resp.getOrDefault("user-message", 
                        resp.getOrDefault("explanation", ""))
                        .toString();
   responseBuilder.message(userMessage);
   ```

#### 1.5 Add Clarification Context Handling
**File**: `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplication.java`

**Changes**:
- When processing a query that follows a clarification, include previous clarification context
- Generate acknowledgment message: "Great! Fetching [description] based on your definition..."

**Note**: This may require conversation context tracking (already available via ChatMemory).

---

### Phase 2: Frontend - Natural Language UI

#### 2.1 Create ClarificationMessage Component
**File**: `services/mill-grinder-ui/src/component/chat/intents/ClarificationMessage.tsx` (NEW)

**Purpose**: Display natural language clarification requests without technical details.

**Features**:
- Show user-message as main text
- Display questions in a friendly list format
- Keep Reply buttons for each question
- No "Step-Back" branding or technical badges
- Clean, conversational styling

**Example**:
```tsx
<Box bg="white" p={16} m={10} style={{borderRadius: 10}}>
  <Text size="md" mb="md">{userMessage}</Text>
  <Stack gap="sm">
    {questions.map((q, idx) => (
      <Group key={idx}>
        <Text size="sm">• {q}</Text>
        <Button size="xs" onClick={() => onReply(q)}>Reply</Button>
      </Group>
    ))}
  </Stack>
</Box>
```

#### 2.2 Update ChatMessageList to Show User Messages
**File**: `services/mill-grinder-ui/src/component/chat/ChatMessageList.tsx`

**Changes**:
1. Extract `user-message` from message content
2. Display user-message as a chat bubble (similar to user messages)
3. Show clarification component when `blocked=true` and `need-clarification=true`
4. Hide StepBackCard for simple queries (when not blocked and no ambiguities shown)

**Logic**:
```tsx
const userMessage = message?.content?.["user-message"];
const blocked = Boolean(message?.content?.blocked);
const needsClarification = Boolean(message?.content?.clarification?.["need-clarification"]);

// Show user message as chat bubble
if (userMessage) {
  return <AssistantMessage text={userMessage} />;
}

// Show clarification component when blocked
if (blocked && needsClarification) {
  return <ClarificationMessage message={message} />;
}

// Show StepBackCard only for debugging/advanced users (optional feature flag)
// Otherwise hide for simple queries
```

#### 2.3 Update StepBackCard for Optional Display
**File**: `services/mill-grinder-ui/src/component/chat/intents/StepBackCard.tsx`

**Changes**:
1. Add `debug` prop (default: false)
2. When `debug=false` and `compact=true`, hide technical details
3. Only show when explicitly enabled (feature flag or user preference)
4. Keep existing functionality for debugging/advanced users

**Alternative**: Move StepBackCard to a collapsible "Details" section that's hidden by default.

#### 2.4 Create AssistantMessage Component
**File**: `services/mill-grinder-ui/src/component/chat/intents/AssistantMessage.tsx` (NEW)

**Purpose**: Display assistant's natural language messages as chat bubbles.

**Features**:
- Similar styling to UserMessage but left-aligned
- Show loading state when processing
- Support markdown formatting for emphasis

---

### Phase 3: Message Flow Improvements

#### 3.1 Simple Query Message Flow
**Backend**:
- Generate: "Sure! Calculating [description]..."
- After SQL execution: Update message to "Here are the results" or keep original

**Frontend**:
- Show message bubble
- Show results below
- Hide StepBackCard (unless debug mode)

#### 3.2 Clarification Message Flow
**Backend**:
- Generate: "To answer this accurately, I need a quick clarification:"
- Include questions in clarification.questions

**Frontend**:
- Show ClarificationMessage component
- User clicks Reply → pre-fills composer
- User sends answer → Backend processes with context
- Backend generates: "Great! Fetching [description] based on your definition..."
- Show new message + results

#### 3.3 Error/Uncertainty Handling
**Backend**:
- Generate: "I want to make sure I answer correctly — could you clarify…?"
- Avoid: "Error: metadata undefined"

**Frontend**:
- Display as natural language, not error alerts
- Use warning styling, not error styling

---

### Phase 4: Tone & Language Consistency

#### 4.1 Update Prompts for Tone
**Files**: All prompt templates in `ai/mill-ai-core/src/main/resources/templates/nlsql/`

**Changes**:
- Add tone guidance to system prompts
- Examples:
  - ✅ "Sure, let me take a look."
  - ✅ "Here's what I found."
  - ✅ "To interpret this correctly, I need a quick clarification."
  - ❌ "Ambiguity detected in intent classification."

#### 4.2 Language Mirroring
**File**: `ai/mill-ai-core/src/main/resources/templates/nlsql/stepback/system.prompt`

**Changes**:
- Emphasize: "Use the same language as the user for all messages and questions"
- Generate user-message in user's language

---

## Implementation Checklist

### Backend Changes
- [ ] Add `user-message` field to StepBackResponse model
- [ ] Add `user-message` to Clarification model
- [ ] Update step-back prompt templates to generate user-message
- [ ] Update ChatApplication to extract/set user-message based on state
- [ ] Update ChatProcessor to prioritize user-message over explanation
- [ ] Add clarification context handling for follow-up messages
- [ ] Update all prompt templates with tone guidance
- [ ] Add tests for user-message generation

### Frontend Changes
- [ ] Create ClarificationMessage component
- [ ] Create AssistantMessage component
- [ ] Update ChatMessageList to show user messages as bubbles
- [ ] Update StepBackCard to be optional/debug-only
- [ ] Hide StepBackCard for simple queries by default
- [ ] Add feature flag for showing StepBackCard (optional)
- [ ] Update styling for natural language messages
- [ ] Test clarification flow end-to-end

### Testing
- [ ] Test simple query flow (invisible step-back)
- [ ] Test clarification flow (natural language)
- [ ] Test after-clarification acknowledgment
- [ ] Test error/uncertainty messages
- [ ] Test language mirroring
- [ ] Test tone consistency

---

## Migration Strategy

### Phase 1: Add User Messages (Non-Breaking)
- Add user-message fields alongside existing explanation
- UI continues to work with explanation as fallback
- No breaking changes

### Phase 2: Update UI (Gradual)
- Deploy new components
- Use feature flag to toggle new UX
- Keep old StepBackCard available for debugging

### Phase 3: Cleanup (After Validation)
- Remove technical details from default view
- Make StepBackCard debug-only
- Update documentation

---

## Open Questions

1. **Feature Flag**: Should StepBackCard visibility be controlled by a feature flag or always hidden for simple queries?
   - **Recommendation**: Always hide for simple queries, add debug mode toggle

2. **Message Updates**: Should user-message be updated after SQL execution, or shown once?
   - **Recommendation**: Show once, results speak for themselves

3. **Clarification Context**: How to track that a message is a clarification response?
   - **Recommendation**: Use conversation context (ChatMemory) to detect previous clarification

4. **Error Messages**: Should verification failures show as clarification requests or errors?
   - **Recommendation**: Convert to clarification questions when possible

---

## Success Criteria

✅ Simple queries show natural language messages, no technical details  
✅ Clarification requests use friendly, conversational tone  
✅ Step-Back is invisible to users for simple queries  
✅ All messages use polite, expert, conversational tone  
✅ No SQL jargon in user-facing messages  
✅ Language mirrors user's language  
✅ Smooth clarification flow with acknowledgment messages  

---

## End of Implementation Plan

