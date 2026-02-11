package com.slack.server.service.impl;

import com.slack.server.model.Conversation;
import com.slack.server.model.Member;
import com.slack.server.model.Workspace;
import com.slack.server.model.event.WebSocketEvent;
import com.slack.server.repository.ConversationRepository;
import com.slack.server.repository.MemberRepository;
import com.slack.server.repository.WorkspaceRepository;
import com.slack.server.service.ConversationService;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public Conversation createConversation(String workspaceId, String memberOneId, String memberTwoId) {
        // Check if conversation already exists
        Conversation existingConversation = conversationRepository
            .findByWorkspaceAndMembers(workspaceId, memberOneId, memberTwoId)
            .orElse(null);

        if (existingConversation != null) {
            return existingConversation;
        }

        Workspace workspace = workspaceRepository.findById(java.util.Objects.requireNonNull(workspaceId))
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        Member memberOne = memberRepository.findById(java.util.Objects.requireNonNull(memberOneId))
            .orElseThrow(() -> new EntityNotFoundException("Member One not found"));

        Member memberTwo = memberRepository.findById(java.util.Objects.requireNonNull(memberTwoId))
            .orElseThrow(() -> new EntityNotFoundException("Member Two not found"));

        Conversation conversation = new Conversation();
        conversation.setWorkspace(workspace);
        conversation.setMemberOne(memberOne);
        conversation.setMemberTwo(memberTwo);
        conversation = conversationRepository.save(conversation);

        // Send WebSocket notification
        WebSocketEvent<Conversation> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.CONVERSATION_CREATED);
        event.setWorkspaceId(workspaceId);
        event.setPayload(conversation);
        webSocketService.sendToWorkspace(workspaceId, event);

        return conversation;
    }

    @Override
    public void deleteConversation(String conversationId) {
        Conversation conversation = getConversationById(java.util.Objects.requireNonNull(conversationId));
        String workspaceId = conversation.getWorkspace().getId();
        conversationRepository.delete(conversation);

        // Send WebSocket notification
        WebSocketEvent<String> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.CONVERSATION_DELETED);
        event.setWorkspaceId(workspaceId);
        event.setConversationId(conversationId);
        event.setPayload(conversationId);
        webSocketService.sendToWorkspace(workspaceId, event);
    }

    @Override
    public Conversation getConversationById(String conversationId) {
        return conversationRepository.findById(java.util.Objects.requireNonNull(conversationId))
            .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
    }

    @Override
    public List<Conversation> getMemberConversations(String workspaceId, String memberId) {
        return conversationRepository.findByWorkspaceIdAndMemberId(
            java.util.Objects.requireNonNull(workspaceId), 
            java.util.Objects.requireNonNull(memberId)
        );
    }

    @Override
    public List<Conversation> getWorkspaceConversations(String workspaceId) {
        return conversationRepository.findByWorkspaceId(java.util.Objects.requireNonNull(workspaceId));
    }
}