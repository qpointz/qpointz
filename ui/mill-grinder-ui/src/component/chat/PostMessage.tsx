import {useEffect, useRef, useState} from "react";
import {ActionIcon, Box, Textarea, Stack} from "@mantine/core";
import {TbSend} from "react-icons/tb";
import {useChatContext} from "./ChatProvider.tsx";
import {StatusIndicator, type StatusIndicatorRef} from "./StatusIndicator.tsx";

export default function PostMessage() {

    const {messages, clarification} = useChatContext();

    const [input, setInput] = useState("");
    const inputRef = useRef<HTMLTextAreaElement>(null);
    const statusIndicatorRef = useRef<StatusIndicatorRef>(null);

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
    }, [messages.postingMessage]);

    // Show notification for posting, loading, or progress events
    useEffect(() => {
        if (messages.postingMessage) {
            // If postingMessage is a string, show it; otherwise show default "Posting..."
            const message = typeof messages.postingMessage === 'string' 
                ? messages.postingMessage 
                : "Posting...";
            statusIndicatorRef.current?.show(message);
        } else if (messages.loading) {
            statusIndicatorRef.current?.show("Getting messages...");
        } else {
            // Hide notification when no active state
            statusIndicatorRef.current?.hide();
        }
    }, [messages.postingMessage, messages.loading]);

    // Prepare clarification mode props for StatusIndicator
    const clarificationMode = clarification.reasoningId && clarification.initialQuestion
        ? {
            type: 'clarification' as const,
            label: clarification.initialQuestion,
            onCancel: clarification.cancel
        }
        : undefined;

    return (
        <Stack gap="xs" mb="md">
            {/* Status Indicator - shows clarification mode */}
            <StatusIndicator
                ref={statusIndicatorRef}
                mode={clarificationMode}
            />
            
            {/* Message Input - ChatGPT style */}
            <Box
                style={{
                    position: 'relative',
                    borderRadius: 12,
                    border: '1px solid #e0e0e0',
                    boxShadow: '0 1px 2px rgba(0, 0, 0, 0.05)',
                    backgroundColor: 'white',
                    padding: '8px 48px 8px 12px',
                }}
            >
                <Textarea
                    variant="unstyled"
                    placeholder="Type your message here..."
                    minRows={1}
                    maxRows={5}
                    autosize
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    onKeyDown={handleCtrlEnterTextArea}
                    disabled={!!messages.postingMessage}
                    ref={inputRef}
                    styles={{
                        input: {
                            padding: 0,
                            fontSize: '14px',
                            lineHeight: '20px',
                            border: 'none',
                            resize: 'none',
                            overflow: 'hidden',
                        }
                    }}
                />
                <ActionIcon
                    disabled={!!messages.postingMessage || !input.trim()}
                    variant="subtle"
                    color="gray"
                    size="lg"
                    radius="xl"
                    onClick={handleButtonClick}
                    style={{
                        position: 'absolute',
                        right: 8,
                        bottom: 8,
                        width: 32,
                        height: 32,
                    }}
                >
                    <TbSend size={16} />
                </ActionIcon>
            </Box>
        </Stack>
    );
}