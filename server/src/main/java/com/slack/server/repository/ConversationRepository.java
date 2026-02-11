package com.slack.server.repository;

import com.slack.server.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.memberOne m1 " +
           "LEFT JOIN FETCH m1.user " +
           "LEFT JOIN FETCH c.memberTwo m2 " +
           "LEFT JOIN FETCH m2.user " +
           "LEFT JOIN FETCH c.workspace " +
           "WHERE c.workspace.id = :workspaceId AND " +
           "((c.memberOne.id = :memberOneId AND c.memberTwo.id = :memberTwoId) OR " +
           "(c.memberOne.id = :memberTwoId AND c.memberTwo.id = :memberOneId))")
    Optional<Conversation> findByWorkspaceAndMembers(String workspaceId, String memberOneId, String memberTwoId);

    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.memberOne m1 " +
           "LEFT JOIN FETCH m1.user " +
           "LEFT JOIN FETCH c.memberTwo m2 " +
           "LEFT JOIN FETCH m2.user " +
           "LEFT JOIN FETCH c.workspace " +
           "WHERE c.workspace.id = :workspaceId")
    List<Conversation> findByWorkspaceId(String workspaceId);
    
    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.memberOne m1 " +
           "LEFT JOIN FETCH m1.user " +
           "LEFT JOIN FETCH c.memberTwo m2 " +
           "LEFT JOIN FETCH m2.user " +
           "LEFT JOIN FETCH c.workspace " +
           "WHERE c.workspace.id = :workspaceId AND " +
           "(c.memberOne.id = :memberId OR c.memberTwo.id = :memberId)")
    List<Conversation> findByWorkspaceIdAndMemberId(String workspaceId, String memberId);

    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.memberOne m1 " +
           "LEFT JOIN FETCH m1.user " +
           "LEFT JOIN FETCH c.memberTwo m2 " +
           "LEFT JOIN FETCH m2.user " +
           "LEFT JOIN FETCH c.workspace " +
           "WHERE c.id = :id")
    Optional<Conversation> findByIdWithMembers(String id);
} 