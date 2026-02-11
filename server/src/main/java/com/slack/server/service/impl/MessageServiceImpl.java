package com.slack.server.service.impl;

import com.slack.server.model.Message;
import com.slack.server.model.Member;
import com.slack.server.model.Channel;
import com.slack.server.model.Conversation;
import com.slack.server.model.event.WebSocketEvent;
import com.slack.server.dto.MessageDTO;
import com.slack.server.repository.MessageRepository;
import com.slack.server.repository.MemberRepository;
import com.slack.server.repository.ChannelRepository;
import com.slack.server.repository.ConversationRepository;
import com.slack.server.service.MessageService;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public Message createMessage(String body, @Nullable String imageId, String workspaceId,
                               String memberId, @Nullable String channelId, @Nullable String conversationId,
                               @Nullable String parentMessageId) {
        Member member = memberRepository.findById(java.util.Objects.requireNonNull(memberId))
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        Message message = new Message();
        message.setBody(java.util.Objects.requireNonNull(body));
        message.setImageId(imageId);
        message.setMember(member);
        message.setWorkspace(member.getWorkspace());

        if (channelId != null) {
            Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));
            message.setChannel(channel);
        }

        if (conversationId != null) {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
            message.setConversation(conversation);
        }

        if (parentMessageId != null) {
            Message parentMessage = messageRepository.findById(parentMessageId)
                .orElseThrow(() -> new EntityNotFoundException("Parent message not found"));
            message.setParentMessage(parentMessage);
        }

        message = messageRepository.save(message);

        // Send WebSocket notification
        WebSocketEvent<MessageDTO> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.MESSAGE_SENT);
        event.setWorkspaceId(java.util.Objects.requireNonNull(workspaceId));
        event.setChannelId(channelId);
        event.setConversationId(conversationId);
        event.setPayload(MessageDTO.fromEntity(message));

        if (channelId != null) {
            webSocketService.sendToChannel(workspaceId, channelId, event);
        } else if (conversationId != null) {
            webSocketService.sendToConversation(workspaceId, conversationId, event);
        }

        return message;
    }

    @Override
    public Message updateMessage(String messageId, String body) {
        Message message = messageRepository.findById(java.util.Objects.requireNonNull(messageId))
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        
        message.setBody(java.util.Objects.requireNonNull(body));
        message.setUpdatedAt(System.currentTimeMillis());
        message = messageRepository.save(message);

        // Send WebSocket notification
        WebSocketEvent<MessageDTO> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.MESSAGE_UPDATED);
        event.setWorkspaceId(message.getWorkspace().getId());
        event.setChannelId(message.getChannel() != null ? message.getChannel().getId() : null);
        event.setConversationId(message.getConversation() != null ? message.getConversation().getId() : null);
        event.setPayload(MessageDTO.fromEntity(message));

        if (message.getChannel() != null) {
            webSocketService.sendToChannel(message.getWorkspace().getId(), message.getChannel().getId(), event);
        } else if (message.getConversation() != null) {
            webSocketService.sendToConversation(message.getWorkspace().getId(), message.getConversation().getId(), event);
        }

        return message;
    }

    @Override
    public Message getMessageById(String messageId) {
        return messageRepository.findById(java.util.Objects.requireNonNull(messageId))
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }

    @Override
    public void deleteMessage(String messageId) {
        Message message = getMessageById(java.util.Objects.requireNonNull(messageId));
        messageRepository.delete(java.util.Objects.requireNonNull(message));

        // Send WebSocket notification
        WebSocketEvent<String> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.MESSAGE_DELETED);
        event.setWorkspaceId(message.getWorkspace().getId());
        event.setChannelId(message.getChannel() != null ? message.getChannel().getId() : null);
        event.setConversationId(message.getConversation() != null ? message.getConversation().getId() : null);
        event.setPayload(messageId);

        if (message.getChannel() != null) {
            webSocketService.sendToChannel(message.getWorkspace().getId(), message.getChannel().getId(), event);
        } else if (message.getConversation() != null) {
            webSocketService.sendToConversation(message.getWorkspace().getId(), message.getConversation().getId(), event);
        }
    }

    @Override
    public Page<Message> getChannelMessages(String channelId, Pageable pageable) {
        return messageRepository.findByChannelIdAndParentMessageIsNullOrderByCreatedAtDesc(java.util.Objects.requireNonNull(channelId), pageable);
    }

    @Override
    public Page<Message> getConversationMessages(String conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdAndParentMessageIsNullOrderByCreatedAtDesc(java.util.Objects.requireNonNull(conversationId), pageable);
    }

    @Override
    public List<Message> getThreadMessages(String parentMessageId) {
        return messageRepository.findByParentMessageId(java.util.Objects.requireNonNull(parentMessageId));
    }

    @Override
    public MessageDTO enrichWithThreadInfo(MessageDTO dto) {
        if (dto == null || dto.getId() == null) return dto;
        long threadCount = messageRepository.countByParentMessageId(dto.getId());
        if (threadCount > 0) {
            dto.setThreadCount((int) threadCount);
            messageRepository.findLatestReplyByParentMessageId(dto.getId()).ifPresent(latestReply -> {
                if (latestReply.getMember() != null && latestReply.getMember().getUser() != null) {
                    dto.setThreadImage(latestReply.getMember().getUser().getImageUrl());
                    dto.setThreadName(latestReply.getMember().getUser().getName());
                }
                dto.setThreadTimestamp(latestReply.getCreatedAt());
            });
        }
        return dto;
    }

    @Override
    public void notifyTyping(String workspaceId, @Nullable String channelId, @Nullable String conversationId, String userId) {
        java.util.Objects.requireNonNull(workspaceId);
        java.util.Objects.requireNonNull(userId);
        if (channelId != null) {
            webSocketService.updateTypingStatus(workspaceId, channelId, userId, true);
        } else if (conversationId != null) {
            webSocketService.updateConversationTypingStatus(workspaceId, conversationId, userId, true);
        }
    }

    @Override
    public void stopTyping(String workspaceId, @Nullable String channelId, @Nullable String conversationId, String userId) {
        if (channelId != null) {
            webSocketService.updateTypingStatus(workspaceId, channelId, userId, false);
        } else if (conversationId != null) {
            webSocketService.updateConversationTypingStatus(workspaceId, conversationId, userId, false);
        }
    }
}