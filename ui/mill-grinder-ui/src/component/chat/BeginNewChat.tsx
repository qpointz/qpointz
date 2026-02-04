import {ActionIcon, Center, Stack, Textarea, Title, Box, Text, Group} from "@mantine/core";
import {TbSend} from "react-icons/tb";
import { useState } from "react";
import {useChatContext} from "./ChatProvider.tsx";
import { RingsLoader } from "./RingsLoader";


export default function BeginNewChat() {
    const [input, setInput] = useState("");
    const {chats, messages} = useChatContext();

    const createChat = () => {
        if (!input.trim()) {
            return;
        }
        chats.create(input);
    }
    
    const isCreating = !!messages.postingMessage;

    const handleCtrlEnterTextArea = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
        if (e.ctrlKey && e.key === "Enter") {
            e.preventDefault();
            createChat()
        }
    }

    const handleButtonClick = (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        createChat();
    }



    // Get the message to display (could be string or boolean)
    const progressText = typeof messages.postingMessage === 'string' 
        ? messages.postingMessage 
        : "Processing...";

    return (
        <Center>
        <Stack w={650} m={50} p={20} gap="md">
            <Title order={3}>New Chat</Title>
            
            {/* Progress notification - transparent */}
            {isCreating && (
                <Group gap="xs" align="center" py={4}>
                    <RingsLoader size={14} />
                    <Text size="xs" c="gray.5">
                        {progressText}
                    </Text>
                </Group>
            )}
            
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
                    disabled={isCreating}
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
                    disabled={isCreating || !input.trim()}
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
        </Center>
    )
}


