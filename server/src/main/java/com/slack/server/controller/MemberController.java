package com.slack.server.controller;

import com.slack.server.model.Member;
import com.slack.server.dto.MemberDTO;
import com.slack.server.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberDTO> addMember(
            @RequestBody @Valid AddMemberRequest request) {
        Member member = memberService.addMember(
            request.getWorkspaceId(),
            request.getUserId(),
            request.getRole()
        );
        return ResponseEntity.ok(MemberDTO.fromEntity(member));
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<MemberDTO> updateMemberRole(
            @PathVariable @NonNull String memberId,
            @RequestBody @Valid UpdateRoleRequest request) {
        Member member = memberService.updateMemberRole(memberId, request.getRole());
        return ResponseEntity.ok(MemberDTO.fromEntity(member));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberDTO> getMember(@PathVariable @NonNull String memberId) {
        Member member = memberService.getMemberById(memberId);
        return ResponseEntity.ok(MemberDTO.fromEntity(member));
    }

    @GetMapping("/workspace/{workspaceId}/user/{userId}")
    public ResponseEntity<MemberDTO> getWorkspaceMember(
            @PathVariable @NonNull String workspaceId,
            @PathVariable @NonNull String userId) {
        Member member = memberService.getMemberByWorkspaceAndUser(workspaceId, userId);
        return ResponseEntity.ok(MemberDTO.fromEntity(member));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable @NonNull String memberId) {
        memberService.removeMember(memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{memberId}/admin")
    public ResponseEntity<Boolean> isMemberAdmin(@PathVariable @NonNull String memberId) {
        boolean isAdmin = memberService.isMemberAdmin(memberId);
        return ResponseEntity.ok(isAdmin);
    }

    public static class AddMemberRequest {
        private @NonNull String workspaceId = "";
        private @NonNull String userId = "";
        private @NonNull Member.Role role = Member.Role.MEMBER;

        public @NonNull String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(@NonNull String workspaceId) { this.workspaceId = workspaceId; }

        public @NonNull String getUserId() { return userId; }
        public void setUserId(@NonNull String userId) { this.userId = userId; }

        public @NonNull Member.Role getRole() { return role; }
        public void setRole(@NonNull Member.Role role) { this.role = role; }
    }

    public static class UpdateRoleRequest {
        private @NonNull Member.Role role = Member.Role.MEMBER;

        public @NonNull Member.Role getRole() { return role; }
        public void setRole(@NonNull Member.Role role) { this.role = role; }
    }
}