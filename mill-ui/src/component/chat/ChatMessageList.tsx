import {Box, ScrollArea, Stack, Text} from "@mantine/core";
import type {ChatMessage} from "../../api/mill";
import GetDataIntent from "./intents/GetDataIntent";
import ExplainIntent from "./intents/ExplainIntent";
import EnrichModelIntent from "./intents/EnrichModelIntent";
import {useRef, useEffect, useState} from "react";
import ChatPostMessage from "./PostMessage.tsx";
import {useChatContext} from "./ChatProvider.tsx";
import UnsupportedIntent from "./intents/UnsupportedIntent.tsx";

export default function ChatMessageListRender() {
    const {messages} = useChatContext();
    const [lastMessageId, setLastMessageId] = useState<string|undefined>('');

    const UserMessage = (message: ChatMessage) => {
        return (
            <Box key={message.id} maw="70%" bg="primary.1" p={10} mb={10}
                 style={{borderRadius: 10, alignSelf: "flex-end"}}>
                <Text>{message.message}</Text>
            </Box>
        )
    }

    const Message = (message: ChatMessage) => {
        if (message.role === "USER") {
            return UserMessage(message);
        }

        if (message.role === "CHAT") {
            const intent: string = message?.content?.resultIntent ?? "";
            switch (intent) {
                case "explain" :
                    return (<ExplainIntent message={message}/>)
                case "get-data" :
                    return (<GetDataIntent message={message}/>)
                case "get-chart" :
                    return (<GetDataIntent message={message}/>)
                case "enrich-model" :
                    return (<EnrichModelIntent message={message}/>)
            }

            return <UnsupportedIntent intent={intent} content={message.content}/>
        }

        return (<Text>{message.role}:</Text>)
    }

    const viewport = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const lastId = messages.list.length === 0
            ? "no-messages"
            : messages.list[0].id;

        if (lastId !== lastMessageId) {
            setLastMessageId(lastId);
        }

        console.log("update list", lastId);

        if (viewport.current) {
            viewport.current.scrollTop = viewport.current.scrollHeight
        }

    }, [messages.list])

    return (
        <Stack p="md" style={{height: "100vh", minHeight: 0}} key={lastMessageId}>
            <ScrollArea w="100%" style={{flex: 1}} viewportRef={viewport}>
                <Stack p="md" style={{minHeight: "100%"}}>
                    {messages.list.map(m => Message(m))}
                </Stack>
            </ScrollArea>
            {messages.postingMessage ? "Posting" : ""}
            <ChatPostMessage/>
        </Stack>
    );
}