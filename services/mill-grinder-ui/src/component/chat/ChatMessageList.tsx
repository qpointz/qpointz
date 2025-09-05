import {Box, Center, Loader, ScrollArea, Stack, Text} from "@mantine/core";
import type {ChatMessage} from "../../api/mill";
import GetDataIntent from "./intents/GetDataIntent";
import ExplainIntent from "./intents/ExplainIntent";
import EnrichModelIntent from "./intents/EnrichModelIntent";
import {useRef, useEffect, useState, useLayoutEffect} from "react";
import ChatPostMessage from "./PostMessage.tsx";
import {useChatContext} from "./ChatProvider.tsx";
import UnsupportedIntent from "./intents/UnsupportedIntent.tsx";

export function ChatMessageListRender() {
    const {messages} = useChatContext();
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
        if (message.role === "USER") {
            return UserMessage(message);
        }

        if (message.role === "CHAT") {
            const intent: string = message?.content?.resultIntent ?? "";
            switch (intent) {
                case "explain" :
                    return (<ExplainIntent key={message.id} message={message}/>)
                case "get-data" :
                    return (<GetDataIntent key={message.id} message={message}/>)
                case "get-chart" :
                    return (<GetDataIntent key={message.id} message={message}/>)
                case "enrich-model" :
                    return (<EnrichModelIntent key={message.id} message={message}/>)
            }

            return <UnsupportedIntent intent={intent} content={message.content}/>
        }

        return (<Text>{message.role}:</Text>)
    }

    const viewport = useRef<HTMLDivElement>(null);

    const scrollToBottom = () =>
        viewport.current!.scrollTo({top: viewport.current!.scrollHeight, behavior: 'smooth'});

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



    }, [messages.list, viewport])

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
            {messages.postingMessage && (
                <Center mb={10} inline>
                    <Loader size="sm" variant="dots" color="blue" mr={20}/>
                    <Box>Analyzing</Box>
                </Center>)}

            <ChatPostMessage/>
        </Stack>
        </Center>
    );

}