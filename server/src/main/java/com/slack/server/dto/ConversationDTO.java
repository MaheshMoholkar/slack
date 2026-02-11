package com.slack.server.dto;

import com.slack.server.model.Conversation;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private String id;
    private MemberSummaryDTO memberOne;
    private MemberSummaryDTO memberTwo;
    private String workspaceId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberSummaryDTO {
        private String id;
        private String role;
        private UserSummaryDTO user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDTO {
        private String id;
        private String name;
        private String imageUrl;
    }

    public static ConversationDTO fromEntity(Conversation conversation) {
        if (conversation == null) {
            return null;
        }

        MemberSummaryDTO memberOneDTO = null;
        if (conversation.getMemberOne() != null) {
            UserSummaryDTO userOneDTO = null;
            if (conversation.getMemberOne().getUser() != null) {
                userOneDTO = new UserSummaryDTO(
                    conversation.getMemberOne().getUser().getId(),
                    conversation.getMemberOne().getUser().getName(),
                    conversation.getMemberOne().getUser().getImageUrl()
                );
            }
            memberOneDTO = new MemberSummaryDTO(
                conversation.getMemberOne().getId(),
                conversation.getMemberOne().getRole() != null ? conversation.getMemberOne().getRole().name() : null,
                userOneDTO
            );
        }

        MemberSummaryDTO memberTwoDTO = null;
        if (conversation.getMemberTwo() != null) {
            UserSummaryDTO userTwoDTO = null;
            if (conversation.getMemberTwo().getUser() != null) {
                userTwoDTO = new UserSummaryDTO(
                    conversation.getMemberTwo().getUser().getId(),
                    conversation.getMemberTwo().getUser().getName(),
                    conversation.getMemberTwo().getUser().getImageUrl()
                );
            }
            memberTwoDTO = new MemberSummaryDTO(
                conversation.getMemberTwo().getId(),
                conversation.getMemberTwo().getRole() != null ? conversation.getMemberTwo().getRole().name() : null,
                userTwoDTO
            );
        }

        return new ConversationDTO(
            conversation.getId(),
            memberOneDTO,
            memberTwoDTO,
            conversation.getWorkspace() != null ? conversation.getWorkspace().getId() : null
        );
    }
}
