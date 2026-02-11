package com.slack.server.service;

import com.slack.server.model.Member;
import java.util.List;

public interface MemberService {
    Member addMember(String workspaceId, String userId, Member.Role role);
    Member updateMemberRole(String memberId, Member.Role role);
    Member getMemberById(String memberId);
    Member getMemberByWorkspaceAndUser(String workspaceId, String userId);
    List<Member> getWorkspaceMembers(String workspaceId);
    List<Member> getWorkspaceAdmins(String workspaceId);
    void removeMember(String memberId);
    boolean isMemberAdmin(String memberId);
}