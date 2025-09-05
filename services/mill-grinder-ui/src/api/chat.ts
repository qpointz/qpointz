import { useCallback, useEffect, useMemo, useState } from "react";
import {
    NlSqlChatControllerApi,
    type Chat,
    type ChatMessage, type CreateChatRequest
} from "./mill/api.ts";
import {useNavigate, useParams} from "react-router";
import {Configuration} from "./mill";

export const useChat = () => {
    const activeChat = useParams<{ chatid?: string }>();

    // Memoized API configuration and instance
    const configuration = useMemo(() => new Configuration(), []);
    const apiInstance = useMemo(
        () => new NlSqlChatControllerApi(configuration),
        [configuration]
    );

    // Chat list state
    const [chatListLoading, setChatListLoading] = useState(false);
    const [chatsList, setChatsList] = useState<Chat[]>([]);

    // Chat message state
    const [messageListLoading, setMessageListLoading] = useState(false);
    const [messageList, setMessageList] = useState<ChatMessage[]>([]);

    const navigate = useNavigate();

    //create chat
    const createChat = useCallback(async (message: string) => {
        const req: CreateChatRequest = {
            name: message
        }
       apiInstance.createChat(req)
           .then((r)=> {
                   navigate('/assist/chat/' + r.data.id)
               });
           //.then(()=> loadChats())
    },  []);

    const deleteChat = useCallback(async (chat: Chat) => {
        apiInstance.deleteChat(chat.id!)
            .then((r)=> {
                loadChats();
            });
    }, []);

    

    // Load list of chats
    const loadChats = useCallback(async () => {
        setChatListLoading(true);
        try {
            const response = await apiInstance.listChats();
            setChatsList(response.data);
        } catch (err) {
            console.error("Failed to load chats", err);
        } finally {
            setChatListLoading(false);
        }
    }, [apiInstance]);

    // Load chat messages when chatid changes
    useEffect(() => {
        const chatId = activeChat?.chatid;
        if (!chatId) {
            setMessageList([]);
            return;
        }

        setMessageListLoading(true);
        apiInstance
            .listChatMessages(chatId)
            .then((r) => setMessageList(r.data))
            .catch((err) => {
                console.error("Failed to load messages", err);
            })
            .finally(() => setMessageListLoading(false));
    }, [activeChat?.chatid, apiInstance, activeChat]);

    // Load chat list on mount or chat change
    useEffect(() => {
        loadChats();
    }, [loadChats]);

    // Final structured return
    return useMemo(
        () => ({
            api: {
                instance: apiInstance,
                configuration
            },
            chats: {
                list: chatsList,
                loading: chatListLoading,
                reload: loadChats,
                active: activeChat,
                create: createChat,
                delete: deleteChat
            },
            messages: {
                list: messageList,
                loading: messageListLoading
            }
        }),
        [
            apiInstance,
            configuration,
            chatsList,
            chatListLoading,
            loadChats,
            activeChat,
            createChat,
            deleteChat,
            messageList,
            messageListLoading
        ]
    );
};
