package com.slack.server.repository;

import com.slack.server.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByChannelIdAndParentMessageIsNullOrderByCreatedAtDesc(String channelId, Pageable pageable);
    Page<Message> findByConversationIdAndParentMessageIsNullOrderByCreatedAtDesc(String conversationId, Pageable pageable);
    List<Message> findByParentMessageId(String parentMessageId);
    long countByParentMessageId(String parentMessageId);
    void deleteByWorkspaceId(String workspaceId);

    @Query("SELECT m FROM Message m LEFT JOIN FETCH m.member mem LEFT JOIN FETCH mem.user WHERE m.parentMessage.id = :parentMessageId ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Message> findLatestReplyByParentMessageId(@Param("parentMessageId") String parentMessageId);
} 