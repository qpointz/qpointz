import {Box, Center, ScrollArea, Stack, Text} from "@mantine/core";
import type {ChatMessage} from "../../api/mill";
import GetDataIntent from "./intents/GetDataIntent";
import ExplainIntent from "./intents/ExplainIntent";
import EnrichModelIntent from "./intents/EnrichModelIntent";
import DoConversationIntent from "./intents/DoConversationIntent";
import ClarificationMessage from "./intents/ClarificationMessage";
import AssistantMessage from "./intents/AssistantMessage";
import {useRef, useEffect, useState, useLayoutEffect, useCallback} from "react";
import ChatPostMessage from "./PostMessage.tsx";
import {useChatContext} from "./ChatProvider.tsx";
import UnsupportedIntent from "./intents/UnsupportedIntent.tsx";

export function ChatMessageListRender() {
    const {messages, clarification} = useChatContext();
    const [lastMessageId, setLastMessageId] = useState<string | undefined>('');

    const UserMessage = (message: ChatMessage) => {
        return (
            <Box key={message.id} maw="70%" bg="primary.1" p={10} mb={10}
                 style={{borderRadius: 10, alignSelf: "flex-end", minWidth: "400px"}}>
                <Text>
                    {(message?.message ?? '').split('\n').map((line, idx, arr) => (
                        <span key={idx}>
                            {line}
                            {idx < arr.length - 1 && <br />}
                        </span>
                    ))}                    
                </Text>
            </Box>
        )
    }

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

    const viewport = useRef<HTMLDivElement>(null);

    const scrollToBottom = useCallback(() => {
        if (viewport.current) {
            viewport.current.scrollTo({top: viewport.current.scrollHeight, behavior: 'smooth'});
        }
    }, []);

    useLayoutEffect(() => {
        requestAnimationFrame(scrollToBottom);
    }, [messages.list.length, scrollToBottom]);


    useEffect(() => {
        const lastId = messages.list.length === 0
            ? "no-messages"
            : messages.list[0].id;

        if (lastId !== lastMessageId) {
            setLastMessageId(lastId);
        }
    }, [messages.list, lastMessageId])

    return (
        <Center>
        <Stack p="md" style={{height: "100vh", minHeight: 0}} w={1024} key={lastMessageId} >
            <ScrollArea w="100%" style={{flex: 1}} viewportRef={viewport}>
                <>
                    <Box style={{minWidth:"100%"}} mx="auto">
                        <Stack p="md" style={{minHeight: "100%"}} gap={"sm"} >
                            {messages.list.map(m => Message(m))}
                        </Stack>
                    </Box>
                </>
            </ScrollArea>
            <ChatPostMessage/>
        </Stack>
        </Center>
    );

}