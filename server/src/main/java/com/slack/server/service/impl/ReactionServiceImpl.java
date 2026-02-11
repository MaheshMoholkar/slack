package com.slack.server.service.impl;

import com.slack.server.model.Reaction;
import com.slack.server.model.Message;
import com.slack.server.model.Member;
import com.slack.server.model.event.WebSocketEvent;
import com.slack.server.dto.ReactionDTO;
import com.slack.server.repository.ReactionRepository;
import com.slack.server.repository.MessageRepository;
import com.slack.server.repository.MemberRepository;
import com.slack.server.service.ReactionService;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReactionServiceImpl implements ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public Reaction addReaction(String messageId, String memberId, String value) {
        Message message = messageRepository.findById(java.util.Objects.requireNonNull(messageId))
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        Member member = memberRepository.findById(java.util.Objects.requireNonNull(memberId))
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // Check if reaction already exists
        Reaction reaction = reactionRepository.findByMessageIdAndMemberIdAndValue(messageId, memberId, java.util.Objects.requireNonNull(value))
            .orElseGet(() -> {
                Reaction newReaction = new Reaction();
                newReaction.setMessage(message);
                newReaction.setMember(member);
                newReaction.setValue(value);
                newReaction.setWorkspace(message.getWorkspace());
                return reactionRepository.save(newReaction);
            });

        // Send WebSocket notification
        WebSocketEvent<ReactionDTO> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.REACTION_ADDED);
        event.setWorkspaceId(message.getWorkspace().getId());
        event.setChannelId(message.getChannel() != null ? message.getChannel().getId() : null);
        event.setConversationId(message.getConversation() != null ? message.getConversation().getId() : null);
        event.setPayload(ReactionDTO.fromEntity(reaction));

        if (message.getChannel() != null) {
            webSocketService.sendToChannel(message.getWorkspace().getId(), message.getChannel().getId(), event);
        } else if (message.getConversation() != null) {
            webSocketService.sendToConversation(message.getWorkspace().getId(), message.getConversation().getId(), event);
        }

        return reaction;
    }

    @Override
    public void removeReaction(String messageId, String memberId, String value) {
        Message message = messageRepository.findById(java.util.Objects.requireNonNull(messageId))
            .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        reactionRepository.deleteByMessageIdAndMemberIdAndValue(messageId, java.util.Objects.requireNonNull(memberId), java.util.Objects.requireNonNull(value));

        // Send WebSocket notification
        WebSocketEvent<Object> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.REACTION_REMOVED);
        event.setWorkspaceId(message.getWorkspace().getId());
        event.setChannelId(message.getChannel() != null ? message.getChannel().getId() : null);
        event.setConversationId(message.getConversation() != null ? message.getConversation().getId() : null);
        event.setPayload(Map.of(
            "messageId", messageId,
            "memberId", memberId,
            "value", value
        ));

        if (message.getChannel() != null) {
            webSocketService.sendToChannel(message.getWorkspace().getId(), message.getChannel().getId(), event);
        } else if (message.getConversation() != null) {
            webSocketService.sendToConversation(message.getWorkspace().getId(), message.getConversation().getId(), event);
        }
    }

    @Override
    public List<Reaction> getMessageReactions(String messageId) {
        return reactionRepository.findByMessageId(java.util.Objects.requireNonNull(messageId));
    }
}