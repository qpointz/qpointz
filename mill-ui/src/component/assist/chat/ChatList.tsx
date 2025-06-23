import {Box, Menu, NavLink, ScrollArea, UnstyledButton, useMantineTheme} from "@mantine/core";
import {TbStar, TbStarFilled, TbTrash} from "react-icons/tb";
import {deleteChat, markChatFavorite, unMarkChatFavorite} from "./mockData.ts";
import type {Chat} from "../../../api/mill";
import {Link} from "react-router";
import { useChat } from "./chat.ts";


export function ChatList() {
    const theme = useMantineTheme();
    const { chats } = useChat();

    const activeChatId = '';// activeChat['*'].replace('chat/','');
    const linkMenu = (chat:Chat) => {
        return (
            <Menu shadow="md" width={200} position="right-start">
                <Menu.Target>
                    <UnstyledButton w={20} h={20}>...</UnstyledButton>
                </Menu.Target>

                <Menu.Dropdown>
                    {chat.isFavorite
                        ? (<Menu.Item onClick={()=> unMarkChatFavorite(chat)} leftSection={<TbStar size={14}/>}>Unmark favorite</Menu.Item>)
                        : (<Menu.Item onClick={()=>markChatFavorite(chat)} leftSection={<TbStarFilled size={14}/>}>Mark favorite</Menu.Item>)
                    }
                    <Menu.Item onClick={()=> deleteChat(chat)} leftSection={<TbTrash color="red" size={14}/>}>Delete</Menu.Item>
                </Menu.Dropdown>
            </Menu>
        )
    }

    return (
        <ScrollArea type="hover" style={{ height: "100%"}}>
            <Box w={350} m={0} pl={10} pr={10} pt={10}

                 style={{ height: "100%", width: "100%", boxSizing: "border-box", borderRight: `1px solid ${theme.colors.gray[3]}` }}>
                <Box mt={6} p={1} bg="transparent" style={{borderRadius: 6}} key="new-chat">
                    <NavLink c="blue" key={"new-chat"} to="/assist/chat" component={Link} label="New Chat+" p={0} m={0}/>
                </Box>
                {chats.list.map((chat: Chat) => (
                    <Box mt={6} p={1} bg={ activeChatId === chat.id ? "gray.3" : "transparent"} style={{borderRadius: 6}} key={chat.id}>
                        <NavLink
                            to={`/assist/chat/${chat.id}`}
                            key={chat.id}
                            component={Link}
                            label={chat.name}
                            p={0} m={0}
                            leftSection={chat.isFavorite ? <TbStar size={14} color={theme.colors.yellow[6]} /> : <></>}
                            rightSection={linkMenu(chat)}
                            style={{
                                borderRadius: 8,
                                marginBottom: 4,
                                background: activeChatId === String(chat.id) ? theme.colors.blue[0] : undefined,
                                transition: 'background 0.2s',
                            }}
                            onMouseEnter={e => e.currentTarget.style.background = theme.colors.gray[1]}
                            onMouseLeave={e => e.currentTarget.style.background = activeChatId === String(chat.id) ? theme.colors.blue[0] : ''}
                        />
                    </Box>
                ))}
            </Box>
        </ScrollArea>
    );
}