package com.slack.server.repository;

import com.slack.server.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByChannelIdOrderByUpdatedAtDesc(String channelId, Pageable pageable);
    Page<Message> findByConversationIdOrderByUpdatedAtDesc(String conversationId, Pageable pageable);
    List<Message> findByParentMessageId(String parentMessageId);
    void deleteByWorkspaceId(String workspaceId);
} 