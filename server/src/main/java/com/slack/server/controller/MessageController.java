package com.slack.server.controller;

import com.slack.server.model.Message;
import com.slack.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> createMessage(
            @RequestBody @Valid CreateMessageRequest request) {
        Message message = messageService.createMessage(
            request.getBody(),
            request.getImageId(),
            request.getWorkspaceId(),
            request.getMemberId(),
            request.getChannelId(),
            request.getConversationId(),
            request.getParentMessageId()
        );
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(
            @PathVariable @NonNull String messageId,
            @RequestBody @Valid UpdateMessageRequest request) {
        Message message = messageService.updateMessage(messageId, request.getBody());
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable @NonNull String messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/channel/{channelId}")
    public ResponseEntity<Page<Message>> getChannelMessages(
            @PathVariable @NonNull String channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Message> messages = messageService.getChannelMessages(
            channelId,
            PageRequest.of(page, size)
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<Message>> getConversationMessages(
            @PathVariable @NonNull String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Message> messages = messageService.getConversationMessages(
            conversationId,
            PageRequest.of(page, size)
        );
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/thread/{parentMessageId}")
    public ResponseEntity<List<Message>> getThreadMessages(
            @PathVariable @NonNull String parentMessageId) {
        List<Message> messages = messageService.getThreadMessages(parentMessageId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable @NonNull String messageId) {
        Message message = messageService.getMessageById(messageId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/typing")
    public ResponseEntity<Void> notifyTyping(
            @RequestBody @Valid TypingRequest request) {
        messageService.notifyTyping(
            request.getWorkspaceId(),
            request.getChannelId(),
            request.getConversationId(),
            request.getUserId()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/typing")
    public ResponseEntity<Void> stopTyping(
            @RequestBody @Valid TypingRequest request) {
        messageService.stopTyping(
            request.getWorkspaceId(),
            request.getChannelId(),
            request.getConversationId(),
            request.getUserId()
        );
        return ResponseEntity.ok().build();
    }

    public static class TypingRequest {
        private @NonNull String workspaceId = "";
        private @Nullable String channelId;
        private @Nullable String conversationId;
        private @NonNull String userId = "";

        public @NonNull String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(@NonNull String workspaceId) { this.workspaceId = workspaceId; }

        public @Nullable String getChannelId() { return channelId; }
        public void setChannelId(@Nullable String channelId) { this.channelId = channelId; }

        public @Nullable String getConversationId() { return conversationId; }
        public void setConversationId(@Nullable String conversationId) { this.conversationId = conversationId; }

        public @NonNull String getUserId() { return userId; }
        public void setUserId(@NonNull String userId) { this.userId = userId; }
    }

    public static class CreateMessageRequest {
        private @NonNull String body = "";
        private @Nullable String imageId;
        private @NonNull String workspaceId = "";
        private @NonNull String memberId = "";
        private @Nullable String channelId;
        private @Nullable String conversationId;
        private @Nullable String parentMessageId;

        public @NonNull String getBody() { return body; }
        public void setBody(@NonNull String body) { this.body = body; }

        public @Nullable String getImageId() { return imageId; }
        public void setImageId(@Nullable String imageId) { this.imageId = imageId; }

        public @NonNull String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(@NonNull String workspaceId) { this.workspaceId = workspaceId; }

        public @NonNull String getMemberId() { return memberId; }
        public void setMemberId(@NonNull String memberId) { this.memberId = memberId; }

        public @Nullable String getChannelId() { return channelId; }
        public void setChannelId(@Nullable String channelId) { this.channelId = channelId; }

        public @Nullable String getConversationId() { return conversationId; }
        public void setConversationId(@Nullable String conversationId) { this.conversationId = conversationId; }

        public @Nullable String getParentMessageId() { return parentMessageId; }
        public void setParentMessageId(@Nullable String parentMessageId) { this.parentMessageId = parentMessageId; }
    }

    public static class UpdateMessageRequest {
        private @NonNull String body = "";

        public @NonNull String getBody() { return body; }
        public void setBody(@NonNull String body) { this.body = body; }
    }
}