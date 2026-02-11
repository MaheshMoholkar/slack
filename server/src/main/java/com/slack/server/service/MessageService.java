package com.slack.server.service;

import com.slack.server.model.Message;
import org.springframework.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MessageService {
    Message createMessage(String body, @Nullable String imageId, String workspaceId, 
                        String memberId, @Nullable String channelId, @Nullable String conversationId, 
                        @Nullable String parentMessageId);
    
    Message updateMessage(String messageId, String body);
    
    Message getMessageById(String messageId);
    
    void deleteMessage(String messageId);
    
    Page<Message> getChannelMessages(String channelId, Pageable pageable);
    
    Page<Message> getConversationMessages(String conversationId, Pageable pageable);
    
    List<Message> getThreadMessages(String parentMessageId);

    // Real-time methods
    void notifyTyping(String workspaceId, @Nullable String channelId, @Nullable String conversationId, String userId);
    void stopTyping(String workspaceId, @Nullable String channelId, @Nullable String conversationId, String userId);
}