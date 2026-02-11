package com.slack.server.service.impl;

import com.slack.server.model.Member;
import com.slack.server.model.User;
import com.slack.server.model.Workspace;
import com.slack.server.model.event.WebSocketEvent;
import com.slack.server.dto.MemberDTO;
import com.slack.server.repository.MemberRepository;
import com.slack.server.repository.UserRepository;
import com.slack.server.repository.WorkspaceRepository;
import com.slack.server.service.MemberService;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public Member addMember(String workspaceId, String userId, Member.Role role) {
        Workspace workspace = workspaceRepository.findById(java.util.Objects.requireNonNull(workspaceId))
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        User user = userRepository.findById(java.util.Objects.requireNonNull(userId))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new IllegalStateException("User is already a member of this workspace");
        }

        Member member = new Member();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(java.util.Objects.requireNonNull(role));
        member = memberRepository.save(member);

        // Send WebSocket notification
        WebSocketEvent<MemberDTO> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.MEMBER_JOINED);
        event.setWorkspaceId(workspaceId);
        event.setPayload(MemberDTO.fromEntity(member));
        webSocketService.sendToWorkspace(workspaceId, event);

        return member;
    }

    @Override
    public Member updateMemberRole(String memberId, Member.Role role) {
        Member member = getMemberById(java.util.Objects.requireNonNull(memberId));
        member.setRole(java.util.Objects.requireNonNull(role));
        member = memberRepository.save(member);

        // Send WebSocket notification
        WebSocketEvent<MemberDTO> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.MEMBER_UPDATED);
        event.setWorkspaceId(member.getWorkspace().getId());
        event.setPayload(MemberDTO.fromEntity(member));
        webSocketService.sendToWorkspace(member.getWorkspace().getId(), event);

        return member;
    }

    @Override
    public Member getMemberById(String memberId) {
        return memberRepository.findById(java.util.Objects.requireNonNull(memberId))
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

    @Override
    public Member getMemberByWorkspaceAndUser(String workspaceId, String userId) {
        return memberRepository.findByWorkspaceIdAndUserId(java.util.Objects.requireNonNull(workspaceId), java.util.Objects.requireNonNull(userId))
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));
    }

    @Override
    public List<Member> getWorkspaceMembers(String workspaceId) {
        return memberRepository.findByWorkspaceId(java.util.Objects.requireNonNull(workspaceId));
    }

    @Override
    public List<Member> getWorkspaceAdmins(String workspaceId) {
        return memberRepository.findByWorkspaceIdAndRole(java.util.Objects.requireNonNull(workspaceId), Member.Role.ADMIN);
    }

    @Override
    public void removeMember(String memberId) {
        Member member = getMemberById(java.util.Objects.requireNonNull(memberId));
        String workspaceId = member.getWorkspace().getId();
        memberRepository.delete(member);

        // Send WebSocket notification
        WebSocketEvent<String> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.MEMBER_LEFT);
        event.setWorkspaceId(workspaceId);
        event.setPayload(memberId);
        webSocketService.sendToWorkspace(workspaceId, event);
    }

    @Override
    public boolean isMemberAdmin(String memberId) {
        Member member = getMemberById(memberId);
        return Member.Role.ADMIN.equals(member.getRole());
    }
}