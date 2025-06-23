import {Box, Group} from "@mantine/core";
import {ChatList} from "./chat/ChatList";
import {Navigate, Route, Routes} from "react-router";
import ChatMessageList from "./chat/ChatMessageList";
import NewChat from "./chat/NewChat.tsx";

export default function AssistLayout() {
    return (
        <Group h="100%" p={0}  align="top">
            <ChatList/>

            <Box flex="1" p={0} mx={0}>
                <Routes>
                    <Route path="chat/:chatid" element={<ChatMessageList />} />
                    <Route path="chat" element={<NewChat />} />
                    <Route index element={<Navigate replace to="chat" />} />
                </Routes>
            </Box>

        </Group>
    )
}