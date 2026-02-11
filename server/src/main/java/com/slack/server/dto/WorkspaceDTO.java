package com.slack.server.dto;

import com.slack.server.model.Workspace;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDTO {
    private String id;
    private String name;
    private String userId;
    private String joinCode;

    public static WorkspaceDTO fromEntity(Workspace workspace) {
        if (workspace == null) {
            return null;
        }
        return new WorkspaceDTO(
            workspace.getId(),
            workspace.getName(),
            workspace.getUserId(),
            workspace.getJoinCode()
        );
    }
}
