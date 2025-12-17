/**
 * Chat Sidebar Content Component
 * 
 * Content displayed in the sidebar when on chat routes.
 * Self-contained with its own ChatProvider.
 */
import { Stack, Text, NavLink, Menu, UnstyledButton } from "@mantine/core";
import { Link, useParams } from "react-router";
import { TbCirclePlus, TbDotsVertical, TbStar, TbStarFilled, TbTrash } from "react-icons/tb";
import { ChatProvider, useChatContext } from "./ChatProvider";
import type { Chat } from "../../api/mill";

function ChatMenu({ chat }: { chat: Chat }) {
    const { chats } = useChatContext();
    
    return (
        <Menu shadow="md" width={200} position="right-start">
            <Menu.Target>
                <UnstyledButton w={20} h={20} onClick={(e) => e.stopPropagation()}>
                    <TbDotsVertical />
                </UnstyledButton>
            </Menu.Target>
            <Menu.Dropdown>
                {chat.isFavorite ? (
                    <Menu.Item 
                        onClick={() => chats.unFavorite(chat)} 
                        leftSection={<TbStar size={14} />}
                    >
                        Unmark favorite
                    </Menu.Item>
                ) : (
                    <Menu.Item 
                        onClick={() => chats.favorite(chat)} 
                        leftSection={<TbStarFilled size={14} />}
                    >
                        Mark favorite
                    </Menu.Item>
                )}
                <Menu.Item 
                    onClick={() => chats.delete(chat)} 
                    leftSection={<TbTrash size={14} />}
                    c="red"
                >
                    Delete
                </Menu.Item>
            </Menu.Dropdown>
        </Menu>
    );
}

function ChatListContent() {
    const { chats } = useChatContext();

    return (
        <Stack gap="xs">
            {/* New Chat Button */}
            <NavLink
                component={Link}
                to="/chat"
                label="New chat"
                leftSection={<TbCirclePlus size={20} />}
            />

            <Text size="sm" c="dimmed" mt="sm">Chats</Text>

            {/* Chat List */}
            <Stack gap={4}>
                {chats.list.map((chat: Chat) => (
                    <NavLink
                        key={chat.id}
                        component={Link}
                        to={`/chat/${chat.id}`}
                        label={chat.name}
                        active={chats.activeId === String(chat.id)}
                        leftSection={chat.isFavorite ? <TbStar size={14} color="var(--mantine-color-yellow-6)" /> : undefined}
                        rightSection={<ChatMenu chat={chat} />}
                    />
                ))}
            </Stack>
        </Stack>
    );
}

export function ChatSidebarContent() {
    const params = useParams<{ chatid?: string }>();
    
    return (
        <ChatProvider chatId={params.chatid}>
            <ChatListContent />
        </ChatProvider>
    );
}
