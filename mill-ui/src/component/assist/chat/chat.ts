import { useCallback, useEffect, useMemo, useState } from "react";
import {
    NlSqlChatControllerApi,
    type Chat,
    type ChatMessage, type CreateChatRequest, type SendChatMessageRequest
} from "../../../api/mill/api.ts";
import {useNavigate, useParams} from "react-router";
import {Configuration} from "../../../api/mill";
import type {AxiosResponse} from "axios";


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
    },  [activeChat?.chatid, apiInstance, activeChat]);

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
            .then((r: AxiosResponse<ChatMessage[], any> ) => setMessageList(r.data))
            .catch((err) => {
                console.error("Failed to load messages", err);
            })
            .finally(() => setMessageListLoading(false));
    }, [activeChat?.chatid, apiInstance, activeChat]);

    // Load chat list on mount or chat change
    useEffect(() => {
        loadChats();
    }, [loadChats]);



    useEffect(() => {
        if (!activeChat?.chatid) {
            return;
        }
        const source = new EventSource(`/api/nl2sql/chats/${activeChat?.chatid}/stream`);
        source.onmessage = (event) => {
            const  data = JSON.parse(event.data);
            if (data?.id == null) {
                return
            }

            if (messageList.some(u=> u.id === data.id)) {
                return;
            }

            messageList.push(data);
            setMessageList(messageList);
            console.log("New message received", data);
        };
    }, [activeChat]);


    const [postingMessage, setPostingMessage] = useState(false);

    const messagePost = useCallback(async (message: string) => {
        const req : SendChatMessageRequest = {
            message: message
        };

        if (!activeChat?.chatid) {
            console.error("No active chat to post message to");
            return;
        }

        setPostingMessage(true);
        apiInstance
            .postChatMessages(activeChat.chatid, req)
            .then((r)=> {
                console.log("Message posted successfully", r.data);
            })
            .finally(() => {
                setPostingMessage(false);
            })
    }, [apiInstance]);




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
                create: createChat
            },
            messages: {
                list: messageList,
                loading: messageListLoading,
                post : messagePost,
                postingMessage : postingMessage
            }
        }),
        [
            apiInstance,
            configuration,
            chatsList,
            chatListLoading,
            loadChats,
            activeChat,
            messageList,
            messageListLoading,
            postingMessage
        ]
    );
};
