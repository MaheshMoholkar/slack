package com.slack.server.controller;

import com.slack.server.model.Workspace;
import com.slack.server.model.Member;
import com.slack.server.dto.MemberDTO;
import com.slack.server.dto.WorkspaceDTO;
import com.slack.server.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceDTO> createWorkspace(
            @RequestBody @Valid CreateWorkspaceRequest request) {
        Workspace workspace = workspaceService.createWorkspace(
            request.getName(),
            request.getUserId()
        );
        return ResponseEntity.ok(WorkspaceDTO.fromEntity(workspace));
    }

    @PutMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDTO> updateWorkspace(
            @PathVariable @NonNull String workspaceId,
            @RequestBody @Valid UpdateWorkspaceRequest request) {
        Workspace workspace = workspaceService.updateWorkspace(workspaceId, request.getName());
        return ResponseEntity.ok(WorkspaceDTO.fromEntity(workspace));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable @NonNull String workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceDTO> getWorkspace(@PathVariable @NonNull String workspaceId) {
        Workspace workspace = workspaceService.getWorkspaceById(workspaceId);
        return ResponseEntity.ok(WorkspaceDTO.fromEntity(workspace));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WorkspaceDTO>> getUserWorkspaces(
            @PathVariable @NonNull String userId) {
        List<Workspace> workspaces = workspaceService.getUserWorkspaces(userId);
        List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                .map(WorkspaceDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<MemberDTO>> getWorkspaceMembers(
            @PathVariable @NonNull String workspaceId) {
        List<Member> members = workspaceService.getWorkspaceMembers(workspaceId);
        List<MemberDTO> memberDTOs = members.stream()
                .map(MemberDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(memberDTOs);
    }

    @PostMapping("/join")
    public ResponseEntity<WorkspaceDTO> joinWorkspace(
            @RequestBody @Valid JoinWorkspaceRequest request) {
        Workspace workspace = workspaceService.joinWorkspace(
            request.getJoinCode(),
            request.getUserId()
        );
        return ResponseEntity.ok(WorkspaceDTO.fromEntity(workspace));
    }

    @PostMapping("/{workspaceId}/regenerate-code")
    public ResponseEntity<String> regenerateJoinCode(@PathVariable @NonNull String workspaceId) {
        String newJoinCode = workspaceService.regenerateJoinCode(workspaceId);
        return ResponseEntity.ok(newJoinCode);
    }

    public static class CreateWorkspaceRequest {
        private @NonNull String name = "";
        private @NonNull String userId = "";

        public @NonNull String getName() { return name; }
        public void setName(@NonNull String name) { this.name = name; }

        public @NonNull String getUserId() { return userId; }
        public void setUserId(@NonNull String userId) { this.userId = userId; }
    }

    public static class UpdateWorkspaceRequest {
        private @NonNull String name = "";

        public @NonNull String getName() { return name; }
        public void setName(@NonNull String name) { this.name = name; }
    }

    public static class JoinWorkspaceRequest {
        private @NonNull String joinCode = "";
        private @NonNull String userId = "";

        public @NonNull String getJoinCode() { return joinCode; }
        public void setJoinCode(@NonNull String joinCode) { this.joinCode = joinCode; }

        public @NonNull String getUserId() { return userId; }
        public void setUserId(@NonNull String userId) { this.userId = userId; }
    }
}