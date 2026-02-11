package com.slack.server.service;

import com.slack.server.model.User;
import org.springframework.lang.Nullable;

public interface AuthService {
    User register(String name, String email, String password);
    String login(String email, String password);
    void logout();
    User getCurrentUser();
    User updateProfile(String userId, String name, @Nullable String imageUrl);
    void changePassword(String userId, String currentPassword, String newPassword);
    boolean validateToken(String token);
}