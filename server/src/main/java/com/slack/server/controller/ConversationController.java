package com.slack.server.controller;

import com.slack.server.model.Conversation;
import com.slack.server.model.Member;
import com.slack.server.model.User;
import com.slack.server.dto.ConversationDTO;
import com.slack.server.repository.MemberRepository;
import com.slack.server.service.ConversationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody @Valid CreateConversationRequest request) {
        Conversation conversation = conversationService.createConversation(
            request.getWorkspaceId(),
            request.getMemberOneId(),
            request.getMemberTwoId()
        );
        return ResponseEntity.ok(ConversationDTO.fromEntity(conversation));
    }

    @PostMapping("/create-or-get")
    public ResponseEntity<ConversationDTO> createOrGetConversation(
            @RequestBody @Valid CreateOrGetRequest request,
            @AuthenticationPrincipal User currentUser) {
        // Resolve the current user's member in the workspace
        Member currentMember = memberRepository.findByWorkspaceIdAndUserId(
                request.getWorkspaceId(), currentUser.getId())
            .orElseThrow(() -> new EntityNotFoundException("You are not a member of this workspace"));

        // Create or get conversation (service is already idempotent)
        Conversation conversation = conversationService.createConversation(
            request.getWorkspaceId(),
            currentMember.getId(),
            request.getMemberId()
        );
        return ResponseEntity.ok(ConversationDTO.fromEntity(conversation));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable @NonNull String conversationId) {
        conversationService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable @NonNull String conversationId) {
        Conversation conversation = conversationService.getConversationById(conversationId);
        return ResponseEntity.ok(ConversationDTO.fromEntity(conversation));
    }

    @GetMapping("/workspace/{workspaceId}/member/{memberId}")
    public ResponseEntity<List<ConversationDTO>> getMemberConversations(
            @PathVariable @NonNull String workspaceId,
            @PathVariable @NonNull String memberId) {
        List<Conversation> conversations = conversationService.getMemberConversations(workspaceId, memberId);
        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(ConversationDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conversationDTOs);
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<ConversationDTO>> getWorkspaceConversations(
            @PathVariable @NonNull String workspaceId) {
        List<Conversation> conversations = conversationService.getWorkspaceConversations(workspaceId);
        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(ConversationDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conversationDTOs);
    }

    public static class CreateConversationRequest {
        private @NonNull String workspaceId = "";
        private @NonNull String memberOneId = "";
        private @NonNull String memberTwoId = "";

        public @NonNull String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(@NonNull String workspaceId) { this.workspaceId = workspaceId; }

        public @NonNull String getMemberOneId() { return memberOneId; }
        public void setMemberOneId(@NonNull String memberOneId) { this.memberOneId = memberOneId; }

        public @NonNull String getMemberTwoId() { return memberTwoId; }
        public void setMemberTwoId(@NonNull String memberTwoId) { this.memberTwoId = memberTwoId; }
    }

    public static class CreateOrGetRequest {
        private @NonNull String workspaceId = "";
        private @NonNull String memberId = "";

        public @NonNull String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(@NonNull String workspaceId) { this.workspaceId = workspaceId; }

        public @NonNull String getMemberId() { return memberId; }
        public void setMemberId(@NonNull String memberId) { this.memberId = memberId; }
    }
}
