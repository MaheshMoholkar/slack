package com.slack.server.service;

import com.slack.server.model.event.WebSocketEvent;

public interface WebSocketService {
    <T> void sendToWorkspace(String workspaceId, WebSocketEvent<T> event);
    <T> void sendToChannel(String workspaceId, String channelId, WebSocketEvent<T> event);
    <T> void sendToConversation(String workspaceId, String conversationId, WebSocketEvent<T> event);
    void updatePresence(String userId, boolean isOnline);
    void updateTypingStatus(String workspaceId, String channelId, String userId, boolean isTyping);
    void updateConversationTypingStatus(String workspaceId, String conversationId, String userId, boolean isTyping);
}