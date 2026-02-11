package com.slack.server.service;

import com.slack.server.model.Reaction;
import java.util.List;

public interface ReactionService {
    Reaction addReaction(String messageId, String memberId, String value);
    void removeReaction(String messageId, String memberId, String value);
    List<Reaction> getMessageReactions(String messageId);
}