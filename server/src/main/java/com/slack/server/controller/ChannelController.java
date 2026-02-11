package com.slack.server.controller;

import com.slack.server.model.Channel;
import com.slack.server.dto.ChannelDTO;
import com.slack.server.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @PostMapping
    public ResponseEntity<ChannelDTO> createChannel(
            @RequestBody @Valid CreateChannelRequest request) {
        Channel channel = channelService.createChannel(
            request.getName(),
            request.getWorkspaceId()
        );
        return ResponseEntity.ok(ChannelDTO.fromEntity(channel));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<ChannelDTO> updateChannel(
            @PathVariable @NonNull String channelId,
            @RequestBody @Valid UpdateChannelRequest request) {
        Channel channel = channelService.updateChannel(channelId, request.getName());
        return ResponseEntity.ok(ChannelDTO.fromEntity(channel));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable @NonNull String channelId) {
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelDTO> getChannel(@PathVariable @NonNull String channelId) {
        Channel channel = channelService.getChannelById(channelId);
        return ResponseEntity.ok(ChannelDTO.fromEntity(channel));
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<ChannelDTO>> getWorkspaceChannels(
            @PathVariable @NonNull String workspaceId) {
        List<Channel> channels = channelService.getWorkspaceChannels(workspaceId);
        List<ChannelDTO> channelDTOs = channels.stream()
                .map(ChannelDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(channelDTOs);
    }

    public static class CreateChannelRequest {
        @jakarta.validation.constraints.Size(min = 1, max = 80, message = "Channel name must be 1-80 characters")
        private @NonNull String name = "";
        private @NonNull String workspaceId = "";

        public @NonNull String getName() { return name; }
        public void setName(@NonNull String name) { this.name = name; }

        public @NonNull String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(@NonNull String workspaceId) { this.workspaceId = workspaceId; }
    }

    public static class UpdateChannelRequest {
        private @NonNull String name = "";

        public @NonNull String getName() { return name; }
        public void setName(@NonNull String name) { this.name = name; }
    }
} 