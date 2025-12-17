import { Box, Card, ScrollArea, Stack, Text } from "@mantine/core";
import type { ChatMessage } from "../../api/mill";
import GetDataIntent from "./intents/GetDataIntent";
import ExplainIntent from "./intents/ExplainIntent";
import EnrichModelIntent from "./intents/EnrichModelIntent";
import DoConversationIntent from "./intents/DoConversationIntent";
import ClarificationMessage from "./intents/ClarificationMessage";
import AssistantMessage from "./intents/AssistantMessage";
import { useRef, useEffect } from "react";
import ChatPostMessage from "./PostMessage.tsx";
import { useChatContext } from "./ChatProvider.tsx";
import UnsupportedIntent from "./intents/UnsupportedIntent.tsx";

export function ChatMessageListRender() {
    const { messages, clarification } = useChatContext();
    const bottomRef = useRef<HTMLDivElement>(null);

    const UserMessage = (message: ChatMessage) => {
        return (
            <Card
                key={message.id}
                maw="70%"
                bg="primary.1"
                p="sm"
                mb="xs"
                shadow="none"
                style={{ alignSelf: "flex-end" }}
            >
                <Text size="sm">
                    {(message?.message ?? '').split('\n').map((line, idx, arr) => (
                        <span key={idx}>
                            {line}
                            {idx < arr.length - 1 && <br />}
                        </span>
                    ))}
                </Text>
            </Card>
        );
    };

    const Message = (message: ChatMessage) => {
        // Priority 1: User messages
        if (message.role === "USER") {
            return UserMessage(message);
        }

        // Priority 2: Clarification requests
        if (message.role === "CHAT") {
            const needClarification = message?.content?.['need-clarification'] === true;
            const hasQuestions = (message?.content?.questions && Array.isArray(message.content.questions) && message.content.questions.length > 0) ||
                                (message?.content?.['step-back']?.questions && Array.isArray(message.content['step-back'].questions) && message.content['step-back'].questions.length > 0);
            
            if (needClarification && hasQuestions) {
                return (
                    <ClarificationMessage 
                        key={message.id} 
                        message={message}
                        onReply={clarification.reply}
                        onCancel={clarification.cancel}
                    />
                );
            }
            
            // Priority 3: Assistant messages (user-message exists)
            const userMessage = message?.content?.['user-message'] || message?.message;
            const intent: string = message?.content?.resultIntent ?? "";
            
            // If user-message exists and no intent or intent is not yet determined, show assistant message
            if (userMessage && (!intent || intent === "")) {
                return (
                    <AssistantMessage 
                        key={message.id} 
                        message={message}
                        isLoading={!!messages.postingMessage}
                    />
                );
            }
            
            // Priority 4: Intent-based messages
            if (intent) {
                switch (intent) {
                    case "explain" :
                        return (<ExplainIntent key={message.id} message={message}/>)
                    case "get-data" :
                        return (<GetDataIntent key={message.id} message={message}/>)
                    case "get-chart" :
                        return (<GetDataIntent key={message.id} message={message}/>)
                    case "enrich-model" :
                        return (<EnrichModelIntent key={message.id} message={message}/>)
                    case "do-conversation" :
                        return (<DoConversationIntent key={message.id} message={message}/>)
                }
            }

            return <UnsupportedIntent intent={intent} content={message.content}/>
        }

        return (<Text>{message.role}:</Text>)
    }

    // Scroll to bottom when messages change, when switching chats, or when posting
    useEffect(() => {
        const timeoutId = setTimeout(() => {
            bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
        return () => clearTimeout(timeoutId);
    }, [messages.list, messages.postingMessage]);

    return (
        <Stack p="md" h="100%" w="100%" style={{ overflow: 'hidden' }}>
            <Box style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
                <ScrollArea h="100%" w="100%" type="auto" offsetScrollbars scrollbarSize={8}>
                    <Stack gap="sm" pr="sm">
                        {messages.list.map(m => Message(m))}
                        <Box ref={bottomRef} />
                    </Stack>
                </ScrollArea>
            </Box>
            <ChatPostMessage />
        </Stack>
    );

}