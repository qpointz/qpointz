import {useEffect, useRef, useState} from "react";
import {ActionIcon, Box, Textarea} from "@mantine/core";
import {TbPlayerPlay, TbPlayerStop} from "react-icons/tb";
import {useChatContext} from "./ChatProvider.tsx";

export default function PostMessage() {

    const {messages} = useChatContext();

    const [input, setInput] = useState("");
    const inputRef = useRef<HTMLTextAreaElement>(null);

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

    useEffect(() => {
        if (!messages.postingMessage) {
            inputRef.current?.focus();
        }
    }, [messages.postingMessage ]);

    return (
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
                ref = {inputRef}
            />
            <ActionIcon disabled={messages.postingMessage}
                        ml={10} w={20}
                        onClick={handleButtonClick}>
                { messages.postingMessage ? (<TbPlayerStop/>) : (<TbPlayerPlay/>) }
            </ActionIcon>
        </Box>
    );
}