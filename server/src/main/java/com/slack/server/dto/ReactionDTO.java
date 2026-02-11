package com.slack.server.dto;

import com.slack.server.model.Reaction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {
    private String id;
    private String value;
    private String workspaceId;
    private String messageId;
    private MemberSummaryDTO member;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSummaryDTO {
        private String id;
        private String userName;
        private String userImage;
    }

    public static ReactionDTO fromEntity(Reaction reaction) {
        if (reaction == null) {
            return null;
        }

        MemberSummaryDTO memberDTO = null;
        if (reaction.getMember() != null) {
            memberDTO = new MemberSummaryDTO(
                reaction.getMember().getId(),
                reaction.getMember().getUserName(),
                reaction.getMember().getUserImage()
            );
        }

        return new ReactionDTO(
            reaction.getId(),
            reaction.getValue(),
            reaction.getWorkspace() != null ? reaction.getWorkspace().getId() : null,
            reaction.getMessage() != null ? reaction.getMessage().getId() : null,
            memberDTO
        );
    }
}
