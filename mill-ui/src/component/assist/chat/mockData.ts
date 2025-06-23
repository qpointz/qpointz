import type {Chat, ChatMessage} from "../../../api/mill";

const mockChatList: Array<Chat> = [
    {"id":"eecdf92d7ea54d98b0b7b13516e21073","name":"Sales Forecast","isFavorite":true,"created":"2025-07-11T13:07:45.265Z"},
    {"id":"3e23245fecfc4a75915a3eebfb650166","name":"Customer Feedback","isFavorite":false,"created":"2025-07-11T13:07:45.265Z"},
];

export function getChatList():Array<Chat> {
    return mockChatList
        .sort((a, b) => a.isFavorite === b.isFavorite ? 0 : a.isFavorite ? -1 : 1)
        //.sort((a, b) => Date.parse(a.created) === Date.parse(b.created) ? 0 : Date.parse(a.created) > Date.parse(b.created) ? -1 : 1)
}

export function deleteChat(chat: Chat) {
    console.log("Deleting chat:", chat.id);
}

export function markChatFavorite(chat: Chat) {
    console.log("Mark favourite chat:", chat.id);
}

export function unMarkChatFavorite(chat: Chat) {
    console.log("Un mark favourite chat:", chat.id);
}

export const chatMessages: Array<ChatMessage> = [];