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

export const ChatProvider: React.FC<{ children: React.ReactNode, chatId?: string }> = ({ children, chatId }) => {
    const navigate = useNavigate();
    const configuration = new Configuration();
    const api = new NlSqlChatControllerApi(configuration);

    const [chatList, setChatList] = useState<Chat[]>([]);
    const [chatListLoading, setChatListLoading] = useState(false);

    const [messageList, setMessageList] = useState<ChatMessage[]>([]);
    const [messageListLoading, setMessageListLoading] = useState(false);
    const [postingMessage, setPostingMessage] = useState(false);

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

    const createChat = useCallback(async (name: string) => {
        const req: CreateChatRequest = { name };
        try {
            const res = await api.createChat(req);
            navigate(`/chat/${res.data.id}`);
        } catch (err) {
            console.error("Failed to create chat", err);
        }
    }, [navigate]);

    const messagePost = useCallback(async (message: string) => {
        if (!chatId) {
            console.error("No active chat ID");
            return;
        }

        const req: SendChatMessageRequest = { message };

        setPostingMessage(true);
        try {
            await api.postChatMessages(chatId, req);
        } catch (err:any) {
            console.error(err);
            showNotification({
                title: 'Post failed',
                message: err.toString(),
                color: 'red',
                icon: <TbRadioactive />,
                autoClose: false, // stays until dismissed
            });

        } finally {
            setPostingMessage(false);
        }
    }, [chatId]);

    useEffect(() => {
        loadChats();
    }, [loadChats]);

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

    useEffect(() => {
        if (!chatId) return;

        const source = new EventSource(`/api/nl2sql/chats/${chatId}/stream`);
        source.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log("new message received", data);
            setMessageList(prev => {
                if (!data?.id || prev.some(m => m.id === data.id)) {
                    console.log("duplilcate message received", data.id);
                    return prev;
                }
                else {
                    console.log("append to received", data.id);
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

export const useChatContext = (): ChatContextType => {
    const ctx = useContext(ChatContext);
    if (!ctx) {
        throw new Error("useChatContext must be used within a ChatProvider");
    }
    return ctx;
};
