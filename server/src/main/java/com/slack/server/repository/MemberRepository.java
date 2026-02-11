package com.slack.server.repository;

import com.slack.server.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    List<Member> findByWorkspaceId(String workspaceId);
    
    @Query("SELECT m FROM Member m WHERE m.workspace.id = :workspaceId AND m.user.id = :userId")
    Optional<Member> findByWorkspaceIdAndUserId(String workspaceId, String userId);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m " +
           "WHERE m.workspace.id = :workspaceId AND m.user.id = :userId")
    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);
    
    @Query("SELECT m FROM Member m WHERE m.workspace.id = :workspaceId AND m.role = :role")
    List<Member> findByWorkspaceIdAndRole(String workspaceId, Member.Role role);
} 