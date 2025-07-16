import {Alert, Box, Button, Group, ScrollArea, Stack, Text, Textarea} from "@mantine/core";
import type {ChatMessage} from "../../../api/mill";
import GetDataIntent from "./intents/GetDataIntent";
import ExplainIntent from "./intents/ExplainIntent";
import EnrichModelIntent from "./intents/EnrichModelIntent";
import {useChat} from "./chat.ts";
import { useState, useRef, useEffect } from "react";
import {CodeHighlight} from "@mantine/code-highlight";

export default function ChatMessageList() {
    const { messages } = useChat();
    const [input, setInput] = useState("");


    const postMessage = async () => {
        if (!input.trim()) {
            return;
        }
        messages.post(input);
        setInput("");
    }

    const handleCtrlEnterTextArea = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
        if (e.ctrlKey && e.key === "Enter") {
            e.preventDefault();
            postMessage();
        }
    }

    const handleButtonClick = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        postMessage();
    }


    const UserMessage = (message: ChatMessage) => {
        return (
                <Box key={message.id} maw="70%" bg="primary.1" p={10} mb={10} style={{borderRadius: 10, alignSelf: "flex-end"}}>
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
                case "explain" : return (<ExplainIntent  message={message}/>)
                case "get-data" : return (<GetDataIntent  message={message}/>)
                case "get-chart" : return (<GetDataIntent  message={message}/>)
                case "enrich-model" : return (<EnrichModelIntent  message={message}/>)
            }

            return (<Group maw="800px" bg="gray.2" p={10} mb={10} style={{borderRadius: 10, alignSelf: "flex-start"}}>
                <Alert title={`${intent} : intent not supported`} color="danger">
                <Box>
                    <CodeHighlight code={JSON.stringify(message.content, null, 2)} language="json"/>
                </Box>
                </Alert>

            </Group>)

        }

        return (<Text>{message.role}:</Text>)
    }

    const viewport = useRef<HTMLDivElement>(null);
    useEffect(() => {
        if (viewport.current) {
            viewport.current.scrollTop = viewport.current.scrollHeight
        }
    }, [messages.list.length])

    return (
        <Stack p="md" style={{ height: "100vh", minHeight: 0 }}>
            <ScrollArea w="100%" style={{ flex: 1 }} viewportRef={viewport}>
                <Stack p="md" style={{ minHeight: "100%" }}>
                    { messages.list.map(m => Message(m)) }
                </Stack>
            </ScrollArea>

                <Box display="flex" h={140} mb={100} p={10} bg="white" style={{borderRadius: 10, height: "100%"}}>
                    <Textarea
                        variant="unstyled"
                        placeholder="Type your message here..."
                        minRows={5}
                        maxRows={5}
                        rows={5}
                        autosize
                        w="100%"
                        h="100%"
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        onKeyDown = {handleCtrlEnterTextArea}
                        disabled = {messages.postingMessage}
                    />
                    <Button disabled={messages.postingMessage} ml={10} w={20} onClick={handleButtonClick}>Post</Button>
                </Box>
        </Stack>


    );
}