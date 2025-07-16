import {useParams} from "react-router";
import {ChatList} from "./ChatList.tsx";
import {ChatProvider} from "./ChatProvider.tsx";
import BeginNewChat from "./BeginNewChat.tsx";
import {Box, Group} from "@mantine/core";
import ChatMessageList from "./ChatMessageList.tsx";

export default function ChatView() {
    const nav = useParams<{ chatid?: string }>();
    return (
      <ChatProvider chatId={nav.chatid}>
          <Group h="100%" p={0}  align="top">
              <ChatList/>
              <Box flex="1" p={0} mx={0}>
                  {nav.chatid ? (<ChatMessageList/>) : (<BeginNewChat/>) }
              </Box>
          </Group>
      </ChatProvider>
    );
}