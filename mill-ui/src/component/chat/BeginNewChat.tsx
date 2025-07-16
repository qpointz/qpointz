import {ActionIcon, Center, Group, Loader, Stack, Textarea, Title} from "@mantine/core";
import {TbPlayerPlay} from "react-icons/tb";
import { useState } from "react";
import {useChatContext} from "./ChatProvider.tsx";


export default function BeginNewChat() {
    const [input, setInput] = useState("");
    const [creatingChat, setCreatingChat] = useState(false);
    const {chats} = useChatContext();

    const createChat = () => {
        if (!input.trim()) {
            return;
        }
        setCreatingChat(true)
        chats.create(input);
    }

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



    return (
        <Stack h={220} m={50} p={20} bg="white" style={{borderRadius: 10, height: "100%"}}>
            { !creatingChat && (
                <>
                    <Title order={3} mb={10}>New Chat</Title>
                    <Group justify="" align="top" >
                    <Textarea
                        variant="unstyled"
                        placeholder="Type your message here..."
                        minRows={5}
                        maxRows={5}
                        w={"90%"}
                        rows={5}
                        autosize
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        onKeyDown = {handleCtrlEnterTextArea}
                    />
                        <ActionIcon onClick={handleButtonClick} ><TbPlayerPlay /></ActionIcon>
                    </Group>
                </>
            )}

            { creatingChat && (
                <Center>
                    <Loader size="sm" variant="dots" color="blue" mr={20} />
                    <Title order={3} mb={10}>Creating Chat...</Title>
                </Center>
            )}
        </Stack>
    )
}


