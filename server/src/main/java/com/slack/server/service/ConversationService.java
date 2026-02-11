package com.slack.server.service;

import com.slack.server.model.Conversation;
import java.util.List;

public interface ConversationService {
    Conversation createConversation(String workspaceId, String memberOneId, String memberTwoId);
    void deleteConversation(String conversationId);
    Conversation getConversationById(String conversationId);
    List<Conversation> getMemberConversations(String workspaceId, String memberId);
    List<Conversation> getWorkspaceConversations(String workspaceId);
}