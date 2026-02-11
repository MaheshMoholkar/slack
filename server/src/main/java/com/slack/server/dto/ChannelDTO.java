package com.slack.server.dto;

import com.slack.server.model.Channel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDTO {
    private String id;
    private String name;
    private String workspaceId;
    private String createdAt;

    public static ChannelDTO fromEntity(Channel channel) {
        if (channel == null) {
            return null;
        }
        return new ChannelDTO(
            channel.getId(),
            channel.getName(),
            channel.getWorkspace() != null ? channel.getWorkspace().getId() : null,
            channel.getCreatedAt() != null ? channel.getCreatedAt().toString() : null
        );
    }
}
