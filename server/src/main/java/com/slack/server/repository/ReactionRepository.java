package com.slack.server.repository;

import com.slack.server.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, String> {
    List<Reaction> findByMessageId(String messageId);
    Optional<Reaction> findByMessageIdAndMemberIdAndValue(String messageId, String memberId, String value);
    void deleteByMessageIdAndMemberIdAndValue(String messageId, String memberId, String value);
    void deleteByWorkspaceId(String workspaceId);
} 