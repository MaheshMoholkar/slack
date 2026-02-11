package com.slack.server.controller;

import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private WebSocketService webSocketService;

    @MessageMapping("/typing")
    public void handleTyping(@Payload TypingEvent event) {
        String workspaceId = event.getWorkspaceId();
        String userId = event.getUserId();
        if (workspaceId == null || userId == null) {
            return;
        }

        String channelId = event.getChannelId();
        String conversationId = event.getConversationId();

        if (channelId != null) {
            webSocketService.updateTypingStatus(
                workspaceId,
                channelId,
                userId,
                event.isTyping()
            );
        } else if (conversationId != null) {
            webSocketService.updateConversationTypingStatus(
                workspaceId,
                conversationId,
                userId,
                event.isTyping()
            );
        }
    }

    @MessageMapping("/presence")
    public void handlePresence(@Payload PresenceEvent event) {
        String userId = event.getUserId();
        if (userId == null) {
            return;
        }
        webSocketService.updatePresence(userId, event.isOnline());
    }

    public static class TypingEvent {
        private @Nullable String workspaceId;
        private @Nullable String channelId;
        private @Nullable String conversationId;
        private @Nullable String userId;
        private boolean typing;

        public @Nullable String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(@NonNull String workspaceId) { this.workspaceId = workspaceId; }
        
        public @Nullable String getChannelId() { return channelId; }
        public void setChannelId(@NonNull String channelId) { this.channelId = channelId; }
        
        public @Nullable String getConversationId() { return conversationId; }
        public void setConversationId(@NonNull String conversationId) { this.conversationId = conversationId; }
        
        public @Nullable String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public boolean isTyping() { return typing; }
        public void setTyping(boolean typing) { this.typing = typing; }
    }

    public static class PresenceEvent {
        private @Nullable String userId;
        private boolean online;

        public @Nullable String getUserId() { return userId; }
        public void setUserId(@NonNull String userId) { this.userId = userId; }
        
        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }
} 