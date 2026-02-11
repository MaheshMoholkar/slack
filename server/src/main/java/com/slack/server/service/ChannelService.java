package com.slack.server.service;

import com.slack.server.model.Channel;
import java.util.List;

public interface ChannelService {
    Channel createChannel(String name, String workspaceId);
    Channel updateChannel(String channelId, String name);
    Channel getChannelById(String channelId);
    List<Channel> getWorkspaceChannels(String workspaceId);
    void deleteChannel(String channelId);
}