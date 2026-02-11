package com.slack.server.controller;

import com.slack.server.model.Channel;
import com.slack.server.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @PostMapping
    public ResponseEntity<Channel> createChannel(
            @RequestBody @Valid CreateChannelRequest request) {
        Channel channel = channelService.createChannel(
            request.getName(),
            request.getWorkspaceId()
        );
        return ResponseEntity.ok(channel);
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable @NonNull String channelId,
            @RequestBody @Valid UpdateChannelRequest request) {
        Channel channel = channelService.updateChannel(channelId, request.getName());
        return ResponseEntity.ok(channel);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable @NonNull String channelId) {
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<Channel> getChannel(@PathVariable @NonNull String channelId) {
        Channel channel = channelService.getChannelById(channelId);
        return ResponseEntity.ok(channel);
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Channel>> getWorkspaceChannels(
            @PathVariable @NonNull String workspaceId) {
        List<Channel> channels = channelService.getWorkspaceChannels(workspaceId);
        return ResponseEntity.ok(channels);
    }

    public static class CreateChannelRequest {
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