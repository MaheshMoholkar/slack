package com.slack.server.controller;

import com.slack.server.model.Reaction;
import com.slack.server.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping
    public ResponseEntity<Reaction> addReaction(
            @RequestBody @Valid AddReactionRequest request) {
        Reaction reaction = reactionService.addReaction(
            request.getMessageId(),
            request.getMemberId(),
            request.getValue()
        );
        return ResponseEntity.ok(reaction);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeReaction(
            @RequestBody @Valid RemoveReactionRequest request) {
        reactionService.removeReaction(
            request.getMessageId(),
            request.getMemberId(),
            request.getValue()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<Reaction>> getMessageReactions(
            @PathVariable @NonNull String messageId) {
        List<Reaction> reactions = reactionService.getMessageReactions(messageId);
        return ResponseEntity.ok(reactions);
    }

    public static class AddReactionRequest {
        private @NonNull String messageId = "";
        private @NonNull String memberId = "";
        private @NonNull String value = "";

        public @NonNull String getMessageId() { return messageId; }
        public void setMessageId(@NonNull String messageId) { this.messageId = messageId; }

        public @NonNull String getMemberId() { return memberId; }
        public void setMemberId(@NonNull String memberId) { this.memberId = memberId; }

        public @NonNull String getValue() { return value; }
        public void setValue(@NonNull String value) { this.value = value; }
    }

    public static class RemoveReactionRequest {
        private @NonNull String messageId = "";
        private @NonNull String memberId = "";
        private @NonNull String value = "";

        public @NonNull String getMessageId() { return messageId; }
        public void setMessageId(@NonNull String messageId) { this.messageId = messageId; }

        public @NonNull String getMemberId() { return memberId; }
        public void setMemberId(@NonNull String memberId) { this.memberId = memberId; }

        public @NonNull String getValue() { return value; }
        public void setValue(@NonNull String value) { this.value = value; }
    }
}