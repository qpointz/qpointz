export interface ConversationRef {
  id: string;
  title: string;
}

export interface ChatReferencesService {
  getConversationsForContext(
    contextType: string,
    contextId: string,
  ): Promise<ConversationRef[]>;
}
