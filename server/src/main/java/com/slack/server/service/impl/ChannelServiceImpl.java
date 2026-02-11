package com.slack.server.service.impl;

import com.slack.server.model.Channel;
import com.slack.server.model.Workspace;
import com.slack.server.model.event.WebSocketEvent;
import com.slack.server.dto.ChannelDTO;
import com.slack.server.repository.ChannelRepository;
import com.slack.server.repository.WorkspaceRepository;
import com.slack.server.service.ChannelService;
import com.slack.server.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
public class ChannelServiceImpl implements ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public Channel createChannel(String name, String workspaceId) {
        Workspace workspace = workspaceRepository.findById(java.util.Objects.requireNonNull(workspaceId))
            .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        if (channelRepository.existsByWorkspaceIdAndName(workspaceId, java.util.Objects.requireNonNull(name))) {
            throw new IllegalStateException("Channel with this name already exists in the workspace");
        }

        Channel channel = new Channel();
        channel.setName(name);
        channel.setWorkspace(workspace);
        channel = channelRepository.save(channel);

        // Send WebSocket notification
        WebSocketEvent<ChannelDTO> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.CHANNEL_CREATED);
        event.setWorkspaceId(workspaceId);
        event.setPayload(ChannelDTO.fromEntity(channel));
        webSocketService.sendToWorkspace(workspaceId, event);

        return channel;
    }

    @Override
    public Channel updateChannel(String channelId, String name) {
        Channel channel = channelRepository.findById(java.util.Objects.requireNonNull(channelId))
            .orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        if (channelRepository.existsByWorkspaceIdAndName(java.util.Objects.requireNonNull(channel.getWorkspace().getId()), java.util.Objects.requireNonNull(name))) {
            throw new IllegalStateException("Channel with this name already exists in the workspace");
        }

        channel.setName(name);
        channel = channelRepository.save(channel);

        // Send WebSocket notification
        WebSocketEvent<ChannelDTO> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.CHANNEL_UPDATED);
        event.setWorkspaceId(channel.getWorkspace().getId());
        event.setChannelId(channelId);
        event.setPayload(ChannelDTO.fromEntity(channel));
        webSocketService.sendToWorkspace(channel.getWorkspace().getId(), event);

        return channel;
    }

    @Override
    public Channel getChannelById(String channelId) {
        return channelRepository.findById(java.util.Objects.requireNonNull(channelId))
            .orElseThrow(() -> new EntityNotFoundException("Channel not found"));
    }

    @Override
    public List<Channel> getWorkspaceChannels(String workspaceId) {
        return channelRepository.findByWorkspaceId(java.util.Objects.requireNonNull(workspaceId));
    }

    @Override
    public void deleteChannel(String channelId) {
        Channel channel = getChannelById(java.util.Objects.requireNonNull(channelId));
        String workspaceId = channel.getWorkspace().getId();
        channelRepository.delete(channel);

        // Send WebSocket notification
        WebSocketEvent<String> event = new WebSocketEvent<>();
        event.setType(WebSocketEvent.EventType.CHANNEL_DELETED);
        event.setWorkspaceId(workspaceId);
        event.setPayload(channelId);
        webSocketService.sendToWorkspace(workspaceId, event);
    }
}