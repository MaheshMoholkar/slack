package com.slack.server.repository;

import com.slack.server.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    Optional<Workspace> findByJoinCode(String joinCode);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.user.id = :userId")
    List<Workspace> findByMembersUserId(@Param("userId") String userId);

    boolean existsByJoinCode(String joinCode);
} 