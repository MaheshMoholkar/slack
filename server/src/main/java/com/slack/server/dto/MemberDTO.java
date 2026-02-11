package com.slack.server.dto;

import com.slack.server.model.Member;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String id;
    private String role;
    private UserDTO user;
    private WorkspaceDTO workspace;

    public static MemberDTO fromEntity(Member member) {
        if (member == null) {
            return null;
        }

        return new MemberDTO(
            member.getId(),
            member.getRole() != null ? member.getRole().name() : null,
            UserDTO.fromEntity(member.getUser()),
            WorkspaceDTO.fromEntity(member.getWorkspace())
        );
    }
}
