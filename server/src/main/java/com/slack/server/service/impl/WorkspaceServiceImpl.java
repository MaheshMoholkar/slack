package com.slack.server.service.impl;

import com.slack.server.model.Channel;
import com.slack.server.model.Workspace;
import com.slack.server.model.Member;
import com.slack.server.model.User;
import com.slack.server.model.event.WebSocketEvent;
import com.slack.server.repository.ChannelRepository;
import com.slack.server.repository.WorkspaceRepository;
import com.slack.server.repository.MemberRepository;
import com.slack.server.repository.UserRepository;
import com.slack.server.service.WorkspaceService;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public Workspace createWorkspace(String name, String userId) {
        User owner = userRepository.findById(java.util.Objects.requireNonNull(userId))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Workspace workspace = new Workspace();
        workspace.setName(java.util.Objects.requireNonNull(name));
        workspace.setUserId(userId);
        workspace.setJoinCode(generateJoinCode());
        workspace = workspaceRepository.save(workspace);

        // Create owner member
        Member ownerMember = new Member();
        ownerMember.setWorkspace(workspace);
        ownerMember.setUser(owner);
        ownerMember.setRole(Member.Role.ADMIN);
        memberRepository.save(ownerMember);

        // Create default "general" channel
        Channel generalChannel = new Channel();
        generalChannel.setName("general");
        generalChannel.setWorkspace(workspace);
        channelRepository.save(generalChannel);

        // Send WebSocket notification
        WebSocketEvent<Workspace> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.WORKSPACE_CREATED);
        event.setWorkspaceId(workspace.getId());
        event.setPayload(workspace);
        webSocketService.sendToWorkspace(workspace.getId(), event);

        return workspace;
    }

    @Override
    public Workspace updateWorkspace(String workspaceId, String name) {
        Workspace workspace = getWorkspaceById(java.util.Objects.requireNonNull(workspaceId));
        workspace.setName(java.util.Objects.requireNonNull(name));
        workspace = workspaceRepository.save(workspace);

        // Send WebSocket notification
        WebSocketEvent<Workspace> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.WORKSPACE_UPDATED);
        event.setWorkspaceId(workspaceId);
        event.setPayload(workspace);
        webSocketService.sendToWorkspace(workspaceId, event);

        return workspace;
    }

    @Override
    public Workspace getWorkspaceById(String workspaceId) {
        return workspaceRepository.findById(java.util.Objects.requireNonNull(workspaceId))
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));
    }

    @Override
    public List<Workspace> getUserWorkspaces(String userId) {
        return workspaceRepository.findByMembersUserId(java.util.Objects.requireNonNull(userId));
    }

    @Override
    public List<Member> getWorkspaceMembers(String workspaceId) {
        return memberRepository.findByWorkspaceId(java.util.Objects.requireNonNull(workspaceId));
    }

    @Override
    public Workspace joinWorkspace(String joinCode, String userId) {
        Workspace workspace = workspaceRepository.findByJoinCode(java.util.Objects.requireNonNull(joinCode))
            .orElseThrow(() -> new EntityNotFoundException("Invalid join code"));

        User user = userRepository.findById(java.util.Objects.requireNonNull(userId))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (memberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)) {
            throw new IllegalStateException("User is already a member of this workspace");
        }

        Member member = new Member();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(Member.Role.MEMBER);
        memberRepository.save(member);

        // Send WebSocket notification
        WebSocketEvent<Member> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.MEMBER_JOINED);
        event.setWorkspaceId(workspace.getId());
        event.setPayload(member);
        webSocketService.sendToWorkspace(workspace.getId(), event);

        return workspace;
    }

    @Override
    public void deleteWorkspace(String workspaceId) {
        Workspace workspace = getWorkspaceById(java.util.Objects.requireNonNull(workspaceId));
        workspaceRepository.delete(java.util.Objects.requireNonNull(workspace));

        // Send WebSocket notification
        WebSocketEvent<String> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.WORKSPACE_DELETED);
        event.setWorkspaceId(workspaceId);
        event.setPayload(workspaceId);
        webSocketService.sendToWorkspace(workspaceId, event);
    }

    @Override
    public String regenerateJoinCode(String workspaceId) {
        Workspace workspace = getWorkspaceById(workspaceId);
        String newJoinCode = generateJoinCode();
        workspace.setJoinCode(newJoinCode);
        workspaceRepository.save(workspace);

        // Send WebSocket notification
        WebSocketEvent<Workspace> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.WORKSPACE_UPDATED);
        event.setWorkspaceId(workspaceId);
        event.setPayload(workspace);
        webSocketService.sendToWorkspace(workspaceId, event);

        return newJoinCode;
    }

    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}