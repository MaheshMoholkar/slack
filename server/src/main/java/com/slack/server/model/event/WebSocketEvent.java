package com.slack.server.model.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent<T> {
    private @Nullable EventType type;
    private @Nullable String workspaceId;
    private @Nullable String channelId;
    private @Nullable String conversationId;
    private @Nullable T payload;

    public enum EventType {
        // Message events
        MESSAGE_SENT,
        MESSAGE_UPDATED,
        MESSAGE_DELETED,
        
        // Channel events
        CHANNEL_CREATED,
        CHANNEL_UPDATED,
        CHANNEL_DELETED,
        
        // Member events
        MEMBER_JOINED,
        MEMBER_UPDATED,
        MEMBER_LEFT,
        
        // Reaction events
        REACTION_ADDED,
        REACTION_REMOVED,
        
        // Conversation events
        CONVERSATION_CREATED,
        CONVERSATION_UPDATED,
        CONVERSATION_DELETED,
        
        // Workspace events
        WORKSPACE_CREATED,
        WORKSPACE_UPDATED,
        WORKSPACE_DELETED,
        
        // Presence events
        PRESENCE_UPDATE,
        
        // Typing events
        TYPING_UPDATE
    }

    public @Nullable EventType getType() {
        return type;
    }

    public void setType(@Nullable EventType type) {
        this.type = type;
    }

    public @Nullable String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(@Nullable String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public @Nullable String getChannelId() {
        return channelId;
    }

    public void setChannelId(@Nullable String channelId) {
        this.channelId = channelId;
    }

    public @Nullable String getConversationId() {
        return conversationId;
    }

    public void setConversationId(@Nullable String conversationId) {
        this.conversationId = conversationId;
    }

    public @Nullable T getPayload() {
        return payload;
    }

    public void setPayload(@Nullable T payload) {
        this.payload = payload;
    }
} 