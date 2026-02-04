import type { ChatService } from '../types/chat';
import { mockChatService } from './mockApi';

// Export the mock service for now
// When you have a real backend, create a new implementation of ChatService
// and swap it here
export const chatService: ChatService = mockChatService;

// Example of how to create a real API service:
/*
import axios from 'axios';

const API_BASE_URL = 'https://your-api.com';

export const realChatService: ChatService = {
  async *sendMessage(conversationId: string, message: string) {
    const response = await fetch(`${API_BASE_URL}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ conversationId, message }),
    });
    
    const reader = response.body?.getReader();
    const decoder = new TextDecoder();
    
    while (reader) {
      const { done, value } = await reader.read();
      if (done) break;
      yield decoder.decode(value);
    }
  },
};
*/
