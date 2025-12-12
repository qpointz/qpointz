/**
 * Chat context and provider for the NL→SQL assistant UI.
 *
 * Responsibilities:
 * - Manage chat list and chat messages state with loading/posting flags
 * - Call OpenAPI backend (create/list chats, list/post messages)
 * - Subscribe to SSE stream for real-time message updates per chat
 * - Expose a typed context API for child components
 */
import React, { createContext, useCallback, useContext, useEffect, useState, useMemo } from "react";
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
        delete: (chat: Chat) => void;
        favorite: (chat: Chat) => void;
        unFavorite: (chat: Chat) => void;
    };
    messages: {
        list: ChatMessage[];
        loading: boolean;
        post: (msg: string) => void;
        postingMessage: boolean | string;
    };
    clarification: {
        reasoningId?: string;
        initialQuestion?: string;
        reply: (message: ChatMessage) => void;
        cancel: () => void;
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
    const configuration = useMemo(() => new Configuration(), []);
    const api = useMemo(() => new NlSqlChatControllerApi(configuration), [configuration]);

    const [chatList, setChatList] = useState<Chat[]>([]);
    const [chatListLoading, setChatListLoading] = useState(false);

    const [messageList, setMessageList] = useState<ChatMessage[]>([]);
    const [messageListLoading, setMessageListLoading] = useState(false);
    const [postingMessage, setPostingMessage] = useState<boolean | string>(false);
    
    // Clarification context state
    const [clarificationContext, setClarificationContext] = useState<{
        reasoningId?: string;
        initialQuestion?: string;
    }>({});
    
    // Track if clarification was explicitly canceled by user to prevent auto-reactivation
    const [clarificationCanceled, setClarificationCanceled] = useState(false);

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
    }, [api]);

    /**
     * Create a new chat, refresh sidebar, and navigate to the created chat.
     */
    const createChat = useCallback(async (name: string) => {
        const req: CreateChatRequest = { name };
        setPostingMessage("Creating new chat...");
        try {
            const res = await api.createChat(req);
            await loadChats();
            navigate(`/chat/${res.data.id}`);
        } catch (err) {
            console.error("Failed to create chat", err);
        } finally {
            setPostingMessage(false);
        }
    }, [navigate, loadChats]);

    const deleteChat = useCallback(async (chat: Chat) => {
        await api.deleteChat(chat.id!);
        await loadChats();
    }, [api, loadChats]);

    const markChatFavorite = useCallback(async (chat: Chat) => {
        await api.updateChat(chat.id!, { chatName: chat.name!, isFavorite: true });
        await loadChats();
    }, [api, loadChats]);
    
    const unMarkChatFavorite = useCallback(async (chat: Chat) => {
        await api.updateChat(chat.id!, { chatName: chat.name!, isFavorite: false });
        await loadChats();
    }, [api, loadChats]);

    /**
     * Post a message to the active chat.
     *
     * Notes:
     * - UI disables input while posting.
     * - Message list is updated by SSE; on post failure we notify the user.
     * - Includes reasoning-id in content if clarification is active.
     */
    const messagePost = useCallback(async (message: string) => {
        if (!chatId) {
            console.error("No active chat ID");
            return;
        }

        const req: SendChatMessageRequest = { 
            message,
            content: clarificationContext.reasoningId ? { 'reasoning-id': clarificationContext.reasoningId } : undefined
        };

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
                // Only reset on error - SSE events control normal progress
                setPostingMessage(false);
            })
    }, [chatId, api, clarificationContext.reasoningId]);

    /**
     * Extract initial user question from message list.
     * Looks for the most recent USER message before the given clarification message.
     */
    const extractInitialQuestion = useCallback((messages: ChatMessage[], clarificationMessageIndex: number): string => {
        // Look backwards from clarification message to find previous USER message
        for (let i = clarificationMessageIndex - 1; i >= 0; i--) {
            if (messages[i]?.role === "USER") {
                return messages[i]?.message || "";
            }
        }
        return "";
    }, []);

    /**
     * Handle clarification reply - reactivate clarification mode for the specific clarification message.
     */
    const handleClarificationReply = useCallback((clarificationMessage: ChatMessage) => {
        const reasoningId = clarificationMessage.content?.['reasoning-id'];
        
        if (reasoningId) {
            // Reset canceled flag when user explicitly clicks Reply
            setClarificationCanceled(false);
            // Find the index of this clarification message in the message list
            const clarificationIndex = messageList.findIndex(m => m.id === clarificationMessage.id);
            
            if (clarificationIndex >= 0) {
                const initialQuestion = extractInitialQuestion(messageList, clarificationIndex);
                setClarificationContext({
                    reasoningId,
                    initialQuestion
                });
            }
        }
    }, [messageList, extractInitialQuestion]);

    /**
     * Handle clarification cancel - clear clarification context to allow fresh query.
     */
    const handleClarificationCancel = useCallback(() => {
        setClarificationContext({});
        setClarificationCanceled(true);
    }, []);

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
    }, [chatId, api]);

    /**
     * Monitor message list for clarification state changes (for initial load).
     * Auto-activates clarification mode if last message requires clarification.
     * But only if clarification wasn't explicitly canceled by the user.
     */
    useEffect(() => {
        if (messageList.length === 0) {
            setClarificationContext({});
            setClarificationCanceled(false);
            return;
        }

        // If clarification was explicitly canceled, don't auto-reactivate
        if (clarificationCanceled) {
            return;
        }

        // Check the most recent CHAT message for clarification state
        let lastChatMessageIndex = -1;
        for (let i = messageList.length - 1; i >= 0; i--) {
            if (messageList[i]?.role === "CHAT") {
                lastChatMessageIndex = i;
                break;
            }
        }
        
        if (lastChatMessageIndex >= 0) {
            const lastChatMessage = messageList[lastChatMessageIndex];
            const needClarification = lastChatMessage?.content?.['need-clarification'] === true;
            const reasoningId = lastChatMessage?.content?.['reasoning-id'];
            
            if (needClarification && reasoningId) {
                // Auto-activate clarification mode
                const initialQuestion = extractInitialQuestion(messageList, lastChatMessageIndex);
                setClarificationContext({
                    reasoningId,
                    initialQuestion
                });
            } else if (!needClarification) {
                // Clear clarification context when clarification is resolved
                setClarificationContext({});
                setClarificationCanceled(false);
            }
        }
    }, [messageList, extractInitialQuestion, clarificationCanceled]);

    /**
     * Subscribe to SSE stream for the active chat; append new messages uniquely.
     * Stream is closed automatically on cleanup or when chatId changes.
     * Also tracks clarification state from incoming messages.
     */
    useEffect(() => {
        if (!chatId) return;

        const source = new EventSource(`/api/nl2sql/chats/${chatId}/stream`);
        
        // Show progress notification on chat_begin_progress_event
        source.addEventListener("chat_begin_progress_event", (event) => {
            const data = JSON.parse(event.data);
            console.log("chat_begin_progress_event - full data:", data);
            console.log("chat_begin_progress_event - entity:", data?.entity);
            console.log("chat_begin_progress_event - message:", data?.message);
            console.log("chat_begin_progress_event - raw event.data:", event.data);
            const progressText = data?.entity || data?.message || "Processing...";            
            setPostingMessage(progressText);
        });

        // Hide progress notification on chat_end_progress_event
        source.addEventListener("chat_end_progress_event", () => {            
            setPostingMessage(false);
        });
        

        source.addEventListener("chat_message_event", (event) => {
            const data = JSON.parse(event.data);
            setMessageList(prev => {
                if (!data?.id || prev.some(m => m.id === data.id)) {
                    return prev;
                }
                
                const updatedList = [...prev, data];
                
                // Track clarification state from incoming CHAT messages
                if (data?.role === "CHAT") {
                    const needClarification = data?.content?.['need-clarification'] === true;
                    const reasoningId = data?.content?.['reasoning-id'];
                    
                    if (needClarification && reasoningId) {
                        // Auto-activate clarification mode when NEW clarification is needed
                        // Reset canceled flag when new clarification arrives
                        setClarificationCanceled(false);
                        // Extract initial question from message list
                        const clarificationIndex = updatedList.length - 1;
                        const initialQuestion = extractInitialQuestion(updatedList, clarificationIndex);
                        setClarificationContext({
                            reasoningId,
                            initialQuestion
                        });
                    } else if (!needClarification) {
                        // Clear clarification context when clarification is resolved
                        setClarificationContext({});
                        setClarificationCanceled(false);
                    }
                }
                
                return updatedList;
            });
        });

        return () => source.close();
    }, [chatId]);
    

    const value: ChatContextType = {
        chats: {
            list: chatList,
            loading: chatListLoading,
            activeId: chatId,
            reload: loadChats,
            create: createChat,
            delete: deleteChat, 
            favorite: markChatFavorite,
            unFavorite: unMarkChatFavorite
        },
        messages: {
            list: messageList,
            loading: messageListLoading,
            post: messagePost,
            postingMessage
        },
        clarification: {
            reasoningId: clarificationContext.reasoningId,
            initialQuestion: clarificationContext.initialQuestion,
            reply: handleClarificationReply,
            cancel: handleClarificationCancel
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
