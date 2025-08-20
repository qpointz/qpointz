/**
 * Chat context and provider for the NL→SQL assistant UI.
 *
 * Responsibilities:
 * - Manage chat list and chat messages state with loading/posting flags
 * - Call OpenAPI backend (create/list chats, list/post messages)
 * - Subscribe to SSE stream for real-time message updates per chat
 * - Expose a typed context API for child components
 */
import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import {
    NlSqlChatControllerApi,
    type Chat,
    type ChatMessage,
    type CreateChatRequest,
    type SendChatMessageRequest
} from "../../api/mill/api.ts";
import { useNavigate } from "react-router";
import type { AxiosResponse } from "axios";
import {Configuration} from "../../api/mill";
import {showNotification} from "@mantine/notifications";
import {TbRadioactive} from "react-icons/tb";

/**
 * Public shape of the chat context consumed by chat UI components.
 */
interface ChatContextType {
    chats: {
        list: Chat[];
        loading: boolean;
        activeId?: string;
        reload: () => void;
        create: (name: string) => void;
    };
    messages: {
        list: ChatMessage[];
        loading: boolean;
        post: (msg: string) => void;
        postingMessage: boolean;
    };
}

const ChatContext = createContext<ChatContextType | undefined>(undefined);

/**
 * Provider that wires routing (current chat id) with API client and SSE stream.
 *
 * Props:
 * - chatId: optional, current active chat. When present, messages will load and
 *   SSE will be opened for live updates; when absent, messages are cleared.
 */
export const ChatProvider: React.FC<{ children: React.ReactNode, chatId?: string }> = ({ children, chatId }) => {
    const navigate = useNavigate();
    const configuration = new Configuration();
    const api = new NlSqlChatControllerApi(configuration);

    const [chatList, setChatList] = useState<Chat[]>([]);
    const [chatListLoading, setChatListLoading] = useState(false);

    const [messageList, setMessageList] = useState<ChatMessage[]>([]);
    const [messageListLoading, setMessageListLoading] = useState(false);
    const [postingMessage, setPostingMessage] = useState(false);

    /**
     * Load list of chats from backend and update sidebar state.
     */
    const loadChats = useCallback(async () => {
        setChatListLoading(true);
        try {
            const response = await api.listChats();
            setChatList(response.data);
        } catch (err) {
            console.error("Failed to load chats", err);
        } finally {
            setChatListLoading(false);
        }
    }, []);

    /**
     * Create a new chat, refresh sidebar, and navigate to the created chat.
     */
    const createChat = useCallback(async (name: string) => {
        const req: CreateChatRequest = { name };
        try {
            const res = await api.createChat(req);
            await loadChats();
            navigate(`/chat/${res.data.id}`);
        } catch (err) {
            console.error("Failed to create chat", err);
        }
    }, [navigate, loadChats]);

    /**
     * Post a message to the active chat.
     *
     * Notes:
     * - UI disables input while posting.
     * - Message list is updated by SSE; on post failure we notify the user.
     */
    const messagePost = useCallback(async (message: string) => {
        if (!chatId) {
            console.error("No active chat ID");
            return;
        }

        const req: SendChatMessageRequest = { message };

        setPostingMessage(true);

        await api
            .postChatMessages(chatId, req)
            .catch( (reason:any) => {
                console.error(reason);
                showNotification({
                    title: 'Post failed',
                    message: reason.toString(),
                    color: 'red',
                    icon: <TbRadioactive />,
                    autoClose: false, // stays until dismissed
                });
            } )
            .finally(()=> {
                setPostingMessage(false);
            })
    }, [chatId]);

    /**
     * Initial load of chats (and refresh on demand via loadChats).
     */
    useEffect(() => {
        loadChats();
    }, [loadChats]);

    /**
     * Load messages when chatId changes. Clear messages if no chat selected.
     */
    useEffect(() => {
        if (!chatId) {
            setMessageList([]);
            return;
        }

        setMessageListLoading(true);
        api.listChatMessages(chatId)
            .then((r: AxiosResponse<ChatMessage[], any>) => setMessageList(r.data))
            .catch((err) => {
                console.error("Failed to load messages", err);
            })
            .finally(() => setMessageListLoading(false));
    }, [chatId]);

    /**
     * Subscribe to SSE stream for the active chat; append new messages uniquely.
     * Stream is closed automatically on cleanup or when chatId changes.
     */
    useEffect(() => {
        if (!chatId) return;

        const source = new EventSource(`/api/nl2sql/chats/${chatId}/stream`);
        source.onmessage = (event) => {
            const data = JSON.parse(event.data);
            setMessageList(prev => {
                if (!data?.id || prev.some(m => m.id === data.id)) {
                    return prev;
                }
                else {
                    return [...prev, data]
                }
            });
        };

        return () => source.close();
    }, [chatId]);

    const value: ChatContextType = {
        chats: {
            list: chatList,
            loading: chatListLoading,
            activeId: chatId,
            reload: loadChats,
            create: createChat
        },
        messages: {
            list: messageList,
            loading: messageListLoading,
            post: messagePost,
            postingMessage
        }
    };

    return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
};

/**
 * Hook to access the chat context. Throws if used outside ChatProvider.
 */
export const useChatContext = (): ChatContextType => {
    const ctx = useContext(ChatContext);
    if (!ctx) {
        throw new Error("useChatContext must be used within a ChatProvider");
    }
    return ctx;
};
