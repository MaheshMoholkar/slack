package com.slack.server.service;

import com.slack.server.model.Workspace;
import com.slack.server.model.Member;
import java.util.List;

public interface WorkspaceService {
    Workspace createWorkspace(String name, String userId);
    Workspace updateWorkspace(String workspaceId, String name);
    Workspace getWorkspaceById(String workspaceId);
    List<Workspace> getUserWorkspaces(String userId);
    List<Member> getWorkspaceMembers(String workspaceId);
    Workspace joinWorkspace(String joinCode, String userId);
    void deleteWorkspace(String workspaceId);
    String regenerateJoinCode(String workspaceId);
}