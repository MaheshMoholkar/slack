package com.slack.server.service.impl;

import com.slack.server.model.event.WebSocketEvent;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public <T> void sendToWorkspace(String workspaceId, WebSocketEvent<T> event) {
        messagingTemplate.convertAndSend("/topic/workspace/" + java.util.Objects.requireNonNull(workspaceId), java.util.Objects.requireNonNull(event));
    }

    @Override
    public <T> void sendToChannel(String workspaceId, String channelId, WebSocketEvent<T> event) {
        messagingTemplate.convertAndSend("/topic/workspace/" + java.util.Objects.requireNonNull(workspaceId) + "/channel/" + java.util.Objects.requireNonNull(channelId), java.util.Objects.requireNonNull(event));
    }

    @Override
    public <T> void sendToConversation(String workspaceId, String conversationId, WebSocketEvent<T> event) {
        messagingTemplate.convertAndSend("/topic/workspace/" + java.util.Objects.requireNonNull(workspaceId) + "/conversation/" + java.util.Objects.requireNonNull(conversationId), java.util.Objects.requireNonNull(event));
    }

    @Override
    public void updatePresence(String userId, boolean isOnline) {
        WebSocketEvent<Map<String, Object>> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.PRESENCE_UPDATE);
        event.setPayload(Map.of(
            "userId", userId,
            "isOnline", isOnline
        ));
        messagingTemplate.convertAndSend("/topic/presence", event);
    }

    @Override
    public void updateTypingStatus(String workspaceId, String channelId, String userId, boolean isTyping) {
        WebSocketEvent<Map<String, Object>> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.TYPING_UPDATE);
        event.setWorkspaceId(workspaceId);
        event.setChannelId(channelId);
        event.setPayload(Map.of(
            "userId", userId,
            "isTyping", isTyping
        ));
        sendToChannel(workspaceId, channelId, event);
    }

    @Override
    public void updateConversationTypingStatus(String workspaceId, String conversationId, String userId, boolean isTyping) {
        WebSocketEvent<Map<String, Object>> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.TYPING_UPDATE);
        event.setWorkspaceId(workspaceId);
        event.setConversationId(conversationId);
        event.setPayload(Map.of(
            "userId", userId,
            "isTyping", isTyping
        ));
        sendToConversation(workspaceId, conversationId, event);
    }
}