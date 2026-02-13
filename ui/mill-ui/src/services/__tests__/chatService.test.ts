import { describe, it, expect } from 'vitest';
import { chatService } from '../chatService';

describe('chatService', () => {
  describe('createChat', () => {
    it('should return chatId and chatName', async () => {
      const result = await chatService.createChat();
      expect(result.chatId).toBeDefined();
      expect(typeof result.chatId).toBe('string');
      expect(result.chatName).toBeDefined();
      expect(typeof result.chatName).toBe('string');
    });

    it('should return unique chatIds on successive calls', async () => {
      const r1 = await chatService.createChat();
      const r2 = await chatService.createChat();
      expect(r1.chatId).not.toBe(r2.chatId);
    });

    it('should use contextLabel in chatName when provided', async () => {
      const result = await chatService.createChat({
        contextType: 'model',
        contextId: 'sales.customers',
        contextLabel: 'Customers table',
      });
      expect(result.chatName).toContain('Customers table');
    });
  });

  describe('sendMessage', () => {
    it('should yield at least one chunk', async () => {
      const { chatId } = await chatService.createChat();
      const chunks: string[] = [];
      for await (const chunk of chatService.sendMessage(chatId, 'Hello')) {
        chunks.push(chunk);
      }
      expect(chunks.length).toBeGreaterThan(0);
    });

    it('chunks should be strings', async () => {
      const { chatId } = await chatService.createChat();
      for await (const chunk of chatService.sendMessage(chatId, 'Hello')) {
        expect(typeof chunk).toBe('string');
      }
    });
  });

  describe('listChats', () => {
    it('should return an array', async () => {
      const chats = await chatService.listChats();
      expect(Array.isArray(chats)).toBe(true);
    });

    it('should include general chats (created without context)', async () => {
      // Create a general chat
      const { chatId } = await chatService.createChat();
      const chats = await chatService.listChats();
      expect(chats.some((c) => c.chatId === chatId)).toBe(true);
    });

    it('each chat summary should have chatId, chatName, updatedAt', async () => {
      await chatService.createChat();
      const chats = await chatService.listChats();
      for (const c of chats) {
        expect(c.chatId).toBeDefined();
        expect(c.chatName).toBeDefined();
        expect(c.updatedAt).toBeDefined();
      }
    });
  });

  describe('getChatByContext', () => {
    it('should return null when no chat exists for the context', async () => {
      const result = await chatService.getChatByContext('model', 'nonexistent');
      expect(result).toBeNull();
    });

    it('should return chatId for a context that has a chat', async () => {
      const { chatId } = await chatService.createChat({
        contextType: 'model',
        contextId: 'test-entity',
        contextLabel: 'Test',
      });
      const found = await chatService.getChatByContext('model', 'test-entity');
      expect(found).toBe(chatId);
    });
  });
});
