package com.slack.server.repository;

import com.slack.server.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, String> {
    List<Channel> findByWorkspaceId(String workspaceId);
    Optional<Channel> findByWorkspaceIdAndName(String workspaceId, String name);
    boolean existsByWorkspaceIdAndName(String workspaceId, String name);
} 