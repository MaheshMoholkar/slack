package com.slack.server.dto;

import com.slack.server.model.Message;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String id;
    private String body;
    private String imageId;
    private MemberSummaryDTO member;
    private String workspaceId;
    private String channelId;
    private String conversationId;
    private String parentMessageId;
    private List<ReactionDTO> reactions;
    private Long createdAt;
    private Long updatedAt;
    private int threadCount;
    private String threadImage;
    private String threadName;
    private Long threadTimestamp;

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

    public static MessageDTO fromEntity(Message message) {
        if (message == null) {
            return null;
        }

        MemberSummaryDTO memberDTO = null;
        if (message.getMember() != null) {
            UserSummaryDTO userDTO = null;
            if (message.getMember().getUser() != null) {
                userDTO = new UserSummaryDTO(
                    message.getMember().getUser().getId(),
                    message.getMember().getUser().getName(),
                    message.getMember().getUser().getImageUrl()
                );
            }
            memberDTO = new MemberSummaryDTO(
                message.getMember().getId(),
                message.getMember().getRole() != null ? message.getMember().getRole().name() : null,
                userDTO
            );
        }

        List<ReactionDTO> reactionDTOs = null;
        if (message.getReactions() != null) {
            reactionDTOs = message.getReactions().stream()
                .map(ReactionDTO::fromEntity)
                .collect(Collectors.toList());
        }

        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setBody(message.getBody());
        dto.setImageId(message.getImageId());
        dto.setMember(memberDTO);
        dto.setWorkspaceId(message.getWorkspace() != null ? message.getWorkspace().getId() : null);
        dto.setChannelId(message.getChannel() != null ? message.getChannel().getId() : null);
        dto.setConversationId(message.getConversation() != null ? message.getConversation().getId() : null);
        dto.setParentMessageId(message.getParentMessage() != null ? message.getParentMessage().getId() : null);
        dto.setReactions(reactionDTOs);
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        // threadCount, threadImage, threadName, threadTimestamp default to 0/null
        return dto;
    }
}
