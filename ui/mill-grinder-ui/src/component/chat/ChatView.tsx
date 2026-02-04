/**
 * Chat View Component
 * 
 * Main view for the chat interface.
 * Sidebar content is rendered via AppSidebar in App.tsx.
 */
import { useParams } from "react-router";
import BeginNewChat from "./BeginNewChat.tsx";
import { Box } from "@mantine/core";
import { ChatMessageListRender as ChatMessageList } from "./ChatMessageList.tsx";

export default function ChatView() {
    const nav = useParams<{ chatid?: string }>();
    return (
        <Box h="100%">
            {nav.chatid ? <ChatMessageList /> : <BeginNewChat />}
        </Box>
    );
}